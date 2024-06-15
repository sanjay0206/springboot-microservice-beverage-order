package com.infybuzz.response;

import com.infybuzz.entity.Order;
import com.infybuzz.entity.OrderStatus;
import lombok.Data;

;

@Data
public class OrderStatusResponse {

    private Long orderId;
    private OrderStatus orderStatus;

    public OrderStatusResponse(Order order) {
        this.orderId = order.getOrderId();
        this.orderStatus = order.getOrderStatus();;
    }
}
