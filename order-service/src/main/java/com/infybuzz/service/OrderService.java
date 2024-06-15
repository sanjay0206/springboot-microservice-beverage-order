package com.infybuzz.service;

import com.infybuzz.entity.Order;
import com.infybuzz.entity.OrderBeverage;
import com.infybuzz.entity.OrderBeverageId;
import com.infybuzz.entity.OrderStatus;
import com.infybuzz.exceptions.OrderServiceException;
import com.infybuzz.exceptions.ResourceNotFoundException;
import com.infybuzz.repository.OrderBeverageRepository;
import com.infybuzz.repository.OrderRepository;
import com.infybuzz.request.BeverageRequest;
import com.infybuzz.request.CreateOrderRequest;
import com.infybuzz.response.BeverageResponse;
import com.infybuzz.response.OrderResponse;
import com.infybuzz.response.OrderStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.infybuzz.entity.OrderStatus.*;

@Service
public class OrderService {
    Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderBeverageRepository orderBeverageRepository;

    @Autowired
    CommonService commonService;

    public OrderResponse getById(long id) {
        logger.info("Inside getById = {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", id));

        // Fetch all OrderBeverage entities associated with the order
        List<OrderBeverage> orderBeverages = orderBeverageRepository.findByOrderId(id);
        List<BeverageResponse> beverageResponses = new ArrayList<>();

        // Retrieve beverage details for each OrderBeverage
        for (OrderBeverage orderBeverage : orderBeverages) {
            BeverageResponse beverageResponse = commonService.getBeverageById(orderBeverage.getId().getBeverageId());
            beverageResponses.add(beverageResponse);
        }

        OrderResponse orderResponse = new OrderResponse(order);
        orderResponse.setBeverages(beverageResponses);
        return orderResponse;
    }

    public List<OrderResponse> getAllOrders() {
        logger.info("Fetching all orders");
        List<Order> orders = orderRepository.findAll();

        List<OrderResponse> orderResponses = new ArrayList<>();
        for (Order order : orders) {
            List<OrderBeverage> orderBeverages = orderBeverageRepository.findByOrderId(order.getOrderId());
            List<BeverageResponse> beverageResponses = new ArrayList<>();

            for (OrderBeverage orderBeverage : orderBeverages) {
                BeverageResponse beverageResponse = commonService.getBeverageById(orderBeverage.getId().getBeverageId());
                beverageResponses.add(beverageResponse);
            }
            OrderResponse orderResponse = new OrderResponse(order);
            orderResponse.setBeverages(beverageResponses);
            orderResponses.add(orderResponse);
        }
        return orderResponses;
    }

    @Transactional(rollbackFor = { SQLException.class })
    public OrderResponse placeOrder(CreateOrderRequest createOrderRequest) {
        logger.info("Inside placeOrder = " + createOrderRequest);

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

            BeverageResponse beverageResponse = commonService.getBeverageById(beverage.getBeverageId());
            logger.info("Quantity = " + beverage.getQuantity() + " | Beverage = " + beverageResponse);

            // If the requested quantity exceeds the available stock, throw an exception
            if (beverageResponse.getAvailability() - beverage.getQuantity() < 0) {
                throw new OrderServiceException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Insufficient availability for beverage ID: " + beverage.getBeverageId());
            }

            // Calculate total cost
            totalCost += beverageResponse.getBeverageCost() * beverage.getQuantity();
            beverages.add(beverageResponse);

            // Create and save OrderBeverage entity
            OrderBeverage orderBeverage = new OrderBeverage();
            orderBeverage.setId(new OrderBeverageId(savedOrder, beverage.getBeverageId()));
            orderBeverage.setQuantity(beverage.getQuantity());
            orderBeverageRepository.save(orderBeverage);

            // Update the available quantity in the Beverage entity through the Beverage service
            commonService.updateBeverageAvailability(beverage.getBeverageId(), beverage.getQuantity());
        }
        logger.info("totalCost = " + totalCost);

        // Update the total cost of the order
        savedOrder.setTotalCost(totalCost);
        orderRepository.save(savedOrder);

        OrderResponse orderResponse = new OrderResponse(savedOrder);
        orderResponse.setBeverages(beverages);
        return orderResponse;
    }

    @Transactional
    public OrderStatusResponse updateOrderStatus(long id, OrderStatus newOrderStatus) {
        logger.info("Inside updateOrder = " + id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", id));

        if (!isValidOrderStatusTransition(newOrderStatus.name(), id)) {
            throw new OrderServiceException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid transition for order status.");
        }
        order.setOrderStatus(newOrderStatus);
        return new OrderStatusResponse(order);
    }

    public void deleteOrder(Long id) {
        logger.info("Inside deleteOrder " + id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", id));

        orderRepository.delete(order);
    }

    private boolean isValidOrderStatusTransition(String newOrderStatus, Long id) {
        OrderStatus currentOrderStatus = orderRepository.findById(id)
                .map(Order::getOrderStatus)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId",  id));
        logger.info("currentOrderStatus = " + currentOrderStatus);

        switch (currentOrderStatus) {
            case PREPARING:
                return Objects.equals(newOrderStatus, SERVED.name()) || Objects.equals(newOrderStatus, CANCELLED.name());
            case SERVED:
                return !Objects.equals(newOrderStatus, CANCELLED.name());
            case CANCELLED:
                return false;
            default:
                return true;
        }
    }
}
