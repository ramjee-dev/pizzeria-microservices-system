package com.pizzastore.order_service.controller;

import com.pizzastore.order_service.dto.OrderDto;
import com.pizzastore.order_service.dto.OrderRequestDto;
import com.pizzastore.order_service.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Management", description = "Operations for managing pizza orders")
@CrossOrigin(origins = "*", maxAge = 3600)
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @PostMapping
    @Operation(summary = "Create new order", description = "Place a new pizza order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid order data")
    })
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderRequestDto orderRequest) {
        logger.info("POST /api/orders - Creating order for user: {}", orderRequest.getUserId());

        OrderDto createdOrder = orderService.createOrder(orderRequest);

        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Retrieve order details by order ID")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long orderId) {
        logger.info("GET /api/orders/{} - Getting order by ID", orderId);

        OrderDto order = orderService.getOrderById(orderId);

        return ResponseEntity.ok(order);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user orders", description = "Retrieve all orders for a specific user")
    public ResponseEntity<List<OrderDto>> getUserOrders(@PathVariable Long userId) {
        logger.info("GET /api/orders/user/{} - Getting orders for user", userId);

        List<OrderDto> orders = orderService.getUserOrders(userId);

        return ResponseEntity.ok(orders);
    }

    // Admin endpoints

    @GetMapping("/admin/all")
    @Operation(summary = "Get all orders (Admin)", description = "Retrieve all orders in the system")
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        logger.info("GET /api/orders/admin/all - Getting all orders");

        List<OrderDto> orders = orderService.getAllOrders();

        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/admin/{orderId}/status")
    @Operation(summary = "Update order status (Admin)", description = "Update the status of an order")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam @Parameter(description = "New order status") String status) {

        logger.info("PATCH /api/orders/admin/{}/status - Updating status to {}", orderId, status);

        OrderDto updatedOrder = orderService.updateOrderStatus(orderId, status);

        return ResponseEntity.ok(updatedOrder);
    }

    @PostMapping("/admin/{orderId}/notify")
    @Operation(summary = "Send custom notification for order", description = "Send custom notification message for specific order")
    public ResponseEntity<String> sendOrderNotification(
            @PathVariable Long orderId,
            @RequestParam String message,
            @RequestParam(defaultValue = "EMAIL") String channel) {

        logger.info("Sending custom notification for order: {}", orderId);

        try {
            OrderDto order = orderService.getOrderById(orderId);
            orderService.publishNotificationRequest(orderId, order.getUserId(), message, channel);

            return ResponseEntity.ok("Custom notification sent successfully");

        } catch (Exception e) {
            logger.error("Error sending notification: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Failed to send notification: " + e.getMessage());
        }
    }

    @GetMapping("/admin/statistics")
    @Operation(summary = "Get order statistics (Admin)", description = "Get order statistics and counts")
    public ResponseEntity<Map<String, Object>> getOrderStatistics() {
        logger.info("GET /api/orders/admin/statistics - Getting order statistics");

        Map<String, Object> stats = orderService.getOrderStatistics();

        return ResponseEntity.ok(stats);
    }
}
