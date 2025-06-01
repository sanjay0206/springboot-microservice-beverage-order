package com.infybuzz.service;

import com.infybuzz.entity.Order;
import com.infybuzz.entity.OrderBeverage;
import com.infybuzz.entity.OrderBeverageId;
import com.infybuzz.entity.OrderStatus;
import com.infybuzz.event.OrderConfirmationEvent;
import com.infybuzz.exceptions.OrderServiceException;
import com.infybuzz.exceptions.ResourceNotFoundException;
import com.infybuzz.external.client.BeverageFeignClient;
import com.infybuzz.repository.OrderBeverageRepository;
import com.infybuzz.repository.OrderRepository;
import com.infybuzz.request.BeverageRequest;
import com.infybuzz.request.CreateOrderRequest;
import com.infybuzz.response.BeverageResponse;
import com.infybuzz.response.OrderResponse;
import com.infybuzz.response.OrderStatusResponse;
import com.infybuzz.response.PagedOrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.infybuzz.entity.OrderStatus.*;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderBeverageRepository orderBeverageRepository;
    private final BeverageFeignClient beverageFeignClient;
    private final KafkaTemplate<String, OrderConfirmationEvent> kafkaTemplate;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        OrderBeverageRepository orderBeverageRepository,
                        BeverageFeignClient beverageFeignClient,
                        KafkaTemplate<String, OrderConfirmationEvent> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.orderBeverageRepository = orderBeverageRepository;
        this.beverageFeignClient = beverageFeignClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public OrderResponse getById(long id) {
        log.info("Inside getById = {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", id));

        // Fetch all OrderBeverage entities associated with the order
        List<OrderBeverage> orderBeverages = orderBeverageRepository.findByOrderId(id);
        List<BeverageResponse> beverageResponses = new ArrayList<>();

        // Retrieve beverage details for each OrderBeverage
        for (OrderBeverage orderBeverage : orderBeverages) {
            BeverageResponse beverageResponse = beverageFeignClient.getById(orderBeverage.getId().getBeverageId());
            beverageResponses.add(beverageResponse);
        }

        OrderResponse orderResponse = new OrderResponse(order);
        orderResponse.setBeverages(beverageResponses);
        return orderResponse;
    }

    public PagedOrderResponse getAllOrders(int pageNo, int pageSize) {
        log.info("Fetching all orders");

        int pageIndex = Math.max(0, pageNo - 1);
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Page<Order> orderPage = orderRepository.findAll(pageable);

        List<OrderResponse> orderResponses = new ArrayList<>();
        for (Order order : orderPage.getContent()) {
            List<OrderBeverage> orderBeverages = orderBeverageRepository.findByOrderId(order.getOrderId());
            List<BeverageResponse> beverageResponses = new ArrayList<>();

            for (OrderBeverage orderBeverage : orderBeverages) {
                BeverageResponse beverageResponse = beverageFeignClient.getById(orderBeverage.getId().getBeverageId());
                beverageResponses.add(beverageResponse);
            }

            OrderResponse orderResponse = new OrderResponse(order);
            orderResponse.setBeverages(beverageResponses);
            orderResponses.add(orderResponse);
        }

        return new PagedOrderResponse(
                orderResponses,
                orderPage.getNumber() + 1,
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.isLast()
        );
    }

    @Transactional(rollbackFor = { SQLException.class })
    public OrderResponse placeOrder(CreateOrderRequest createOrderRequest) {
        log.info("Inside placeOrder = {}", createOrderRequest);

        Order order = new Order();
        order.setUserId(createOrderRequest.getUserId());
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus(PREPARING);
        order.setTotalCost(0.0);

        // Save the order to generate its ID
        Order savedOrder = orderRepository.save(order);
        List<BeverageResponse> beverages = new ArrayList<>();
        double totalCost = 0.0;

        for (BeverageRequest beverage : createOrderRequest.getBeverages()) {
            BeverageResponse beverageResponse = beverageFeignClient.getById(beverage.getBeverageId());
            log.info("Quantity = {} | Beverage = {}", beverage.getQuantity(), beverageResponse);

            // Check if requested quantity exceeds the available stock
            int availableStock = beverageResponse.getAvailability();
            int requestedQuantity = beverage.getQuantity();
            if (availableStock - requestedQuantity < 0) {
                throw new OrderServiceException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "Insufficient availability for beverage ID: " + beverage.getBeverageId());
            }

            // Calculate total cost
            totalCost = (totalCost + beverageResponse.getBeverageCost()) * beverage.getQuantity();
            beverages.add(beverageResponse);

            // Create and save OrderBeverage entity
            OrderBeverage orderBeverage = new OrderBeverage();
            orderBeverage.setId(new OrderBeverageId(savedOrder, beverage.getBeverageId()));
            orderBeverage.setQuantity(beverage.getQuantity());
            orderBeverageRepository.save(orderBeverage);

            // Update the available quantity of beverages
            beverageFeignClient.updateBeverageAvailability(beverage.getBeverageId(), beverage.getQuantity());
        }
        log.info("totalCost = {}", totalCost);

        // Update the total cost of the order
        savedOrder.setTotalCost(totalCost);
        savedOrder = orderRepository.save(savedOrder);

        // Publish booking completed event to orderConfirmationTopic
//        OrderConfirmationEvent orderConfirmationEvent = new OrderConfirmationEvent(
//                savedOrder.getOrderId(),
//                savedOrder.getOrderStatus().name());
//        log.info("Sending event to orderConfirmationTopic with event {}", orderConfirmationEvent);
//
//        // Send the event using kafka template to orderConfirmationTopic
//        kafkaTemplate.send("orderConfirmationTopic", orderConfirmationEvent);

        OrderResponse orderResponse = new OrderResponse(savedOrder);
        orderResponse.setBeverages(beverages);
        return orderResponse;
    }

    @Transactional
    public OrderStatusResponse updateOrderStatus(long id, OrderStatus newOrderStatus) {
        log.info("Inside updateOrder = {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", id));

        if (!isValidOrderStatusTransition(newOrderStatus.name(), id)) {
            throw new OrderServiceException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid transition for order status.");
        }
        order.setOrderStatus(newOrderStatus);
        return new OrderStatusResponse(order);
    }

    public void deleteOrder(Long id) {
        log.info("Inside deleteOrder {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", id));

        orderRepository.delete(order);
    }

    private boolean isValidOrderStatusTransition(String newOrderStatus, Long id) {
        OrderStatus currentOrderStatus = orderRepository.findById(id)
                .map(Order::getOrderStatus)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId",  id));
        log.info("currentOrderStatus = {}", currentOrderStatus);

        return switch (currentOrderStatus) {
            // Can transition from PREPARING to SERVED or CANCELLED
            case PREPARING -> Objects.equals(newOrderStatus, SERVED.name()) || Objects.equals(newOrderStatus, CANCELLED.name());

            // No further transitions allowed from SERVED except remaining as SERVED
            case SERVED -> Objects.equals(newOrderStatus, SERVED.name());

            // Cannot transition from CANCELLED to any other state
            case CANCELLED -> false;
        };
    }
}
