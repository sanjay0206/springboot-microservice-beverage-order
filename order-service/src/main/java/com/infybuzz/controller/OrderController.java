package com.infybuzz.controller;

import com.infybuzz.entity.OrderStatus;
import com.infybuzz.request.CreateOrderRequest;
import com.infybuzz.response.OrderResponse;
import com.infybuzz.response.OrderStatusResponse;
import com.infybuzz.response.PagedOrderResponse;
import com.infybuzz.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@Tag(name = "Order API", description = "Endpoints for managing orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Get all orders (paginated)", description = "Accessible by SHOP_OWNER")
    @ApiResponse(responseCode = "200", description = "Orders fetched successfully")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    @GetMapping
    public ResponseEntity<PagedOrderResponse> getAllOrders(
            @Parameter(description = "Page number") @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @Parameter(description = "Page size") @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize) {
        return ResponseEntity.ok(orderService.getAllOrders(pageNo, pageSize));
    }

    @Operation(summary = "Get order by ID", description = "Accessible by USER")
    @ApiResponse(responseCode = "200", description = "Order details returned")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @Operation(summary = "Place a new order", description = "Accessible by USER")
    @ApiResponse(responseCode = "200", description = "Order placed successfully")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/place")
    public OrderResponse placeOrder(@RequestBody CreateOrderRequest createOrderRequest) {
        return orderService.placeOrder(createOrderRequest);
    }

    @Operation(summary = "Update order status", description = "Accessible by USER or SHOP_OWNER")
    @ApiResponse(responseCode = "200", description = "Order status updated")
    @PreAuthorize("hasAnyRole('USER', 'SHOP_OWNER')")
    @PutMapping("/update/{id}")
    public ResponseEntity<OrderStatusResponse> updateOrderStatus(
            @PathVariable Long id,
            @Parameter(description = "New order status") @RequestParam OrderStatus orderStatus) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, orderStatus));
    }

    @Operation(summary = "Delete an order", description = "Accessible by SHOP_OWNER")
    @ApiResponse(responseCode = "204", description = "Order deleted successfully")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
