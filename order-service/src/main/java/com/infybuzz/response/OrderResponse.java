package com.infybuzz.response;


import com.infybuzz.entity.Order;
import com.infybuzz.entity.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long orderId;
    private Long userId;
    private Double totalCost;
    private OrderStatus orderStatus;
    private LocalDateTime orderDate;
    private List<BeverageResponse> beverages;

    public OrderResponse(Order order) {
        this.orderId = order.getOrderId();
        this.userId = order.getUserId();
        this.orderStatus = order.getOrderStatus();
        this.orderDate = order.getOrderDate();
        this.totalCost = order.getTotalCost();
    }
}
