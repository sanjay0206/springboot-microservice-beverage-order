package com.infybuzz.controller;

import com.infybuzz.entity.OrderStatus;
import com.infybuzz.request.CreateOrderRequest;
import com.infybuzz.response.OrderResponse;
import com.infybuzz.response.OrderStatusResponse;
import com.infybuzz.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    OrderService orderService;

    @GetMapping("/getAll")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @PostMapping("/place")
    public OrderResponse placeOrder(@RequestBody CreateOrderRequest createOrderRequest) {
        return orderService.placeOrder(createOrderRequest);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<OrderStatusResponse> updateOrderStatus(@PathVariable Long id, @RequestParam OrderStatus orderStatus) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, orderStatus));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
