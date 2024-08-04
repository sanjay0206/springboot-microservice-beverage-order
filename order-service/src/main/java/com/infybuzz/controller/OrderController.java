package com.infybuzz.controller;

import com.infybuzz.entity.OrderStatus;
import com.infybuzz.request.CreateOrderRequest;
import com.infybuzz.response.OrderResponse;
import com.infybuzz.response.OrderStatusResponse;
import com.infybuzz.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    OrderService orderService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/getById/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    @GetMapping("/getAll")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/place")
    public OrderResponse placeOrder(@RequestBody CreateOrderRequest createOrderRequest) {
        return orderService.placeOrder(createOrderRequest);
    }

    @PreAuthorize("hasAnyRole('USER', 'SHOP_OWNER')")
    @PutMapping("/update/{id}")
    public ResponseEntity<OrderStatusResponse> updateOrderStatus(@PathVariable Long id, @RequestParam OrderStatus orderStatus) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, orderStatus));
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
