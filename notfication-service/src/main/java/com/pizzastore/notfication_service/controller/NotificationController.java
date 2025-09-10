package com.pizzastore.notfication_service.controller;

import com.pizzastore.notfication_service.model.NotificationEvent;
import com.pizzastore.notfication_service.model.NotificationRequest;
import com.pizzastore.notfication_service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification Management", description = "Operations for sending and managing notifications")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private StreamBridge streamBridge;

    @PostMapping("/send")
    @Operation(summary = "Send notification", description = "Send a notification via specified channel")
    @ApiResponse(responseCode = "200", description = "Notification sent successfully")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationRequest request) {

        logger.info("Sending notification via API: {}", request);

        try {
            notificationService.sendNotification(
                    request.getEventType(),
                    request.getUserId(),
                    request.getMessage(),
                    request.getChannel()
            );

            return ResponseEntity.ok("Notification sent successfully");

        } catch (Exception e) {
            logger.error("Failed to send notification: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Failed to send notification: " + e.getMessage());
        }
    }

    @PostMapping("/publish-event")
    @Operation(summary = "Publish notification event", description = "Publish a notification event to message queue")
    public ResponseEntity<String> publishNotificationEvent(@RequestBody NotificationEvent notification) {

        logger.info("Publishing notification event: {}", notification);

        try {
            streamBridge.send("processNotificationRequests-out-0", notification);
            return ResponseEntity.ok("Notification event published successfully");

        } catch (Exception e) {
            logger.error("Failed to publish notification event: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Failed to publish event: " + e.getMessage());
        }
    }

    @PostMapping("/order-notification")
    @Operation(summary = "Send order notification", description = "Send order-specific notification")
    public ResponseEntity<String> sendOrderNotification(
            @RequestParam String orderId,
            @RequestParam String userId,
            @RequestParam String eventType,
            @RequestParam(defaultValue = "EMAIL") String channel) {

        logger.info("Sending order notification: orderId={}, userId={}, eventType={}",
                orderId, userId, eventType);

        try {
            NotificationEvent notification = new NotificationEvent();
            notification.setEventType(eventType);
            notification.setUserId(userId);
            notification.setOrderId(orderId);
            notification.setChannel(channel);
            notification.setEventId("ORD-" + orderId + "-" + System.currentTimeMillis());

            // Generate appropriate message based on event type
            String message = generateOrderMessage(eventType, orderId);
            notification.setMessage(message);

            notificationService.processNotification(notification);

            return ResponseEntity.ok("Order notification sent successfully");

        } catch (Exception e) {
            logger.error("Failed to send order notification: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Failed to send order notification: " + e.getMessage());
        }
    }

    // Helper method to generate order-specific messages
    private String generateOrderMessage(String eventType, String orderId) {
        return switch (eventType) {
            case "ORDER_CREATED" -> String.format("🍕 Your pizza order #%s has been received!", orderId);
            case "ORDER_CONFIRMED" -> String.format("✅ Order #%s confirmed and being prepared.", orderId);
            case "ORDER_READY" -> String.format("🎉 Order #%s is ready for pickup/delivery!", orderId);
            case "ORDER_DELIVERED" -> String.format("📦 Order #%s delivered successfully!", orderId);
            default -> String.format("📱 Update for order #%s: %s", orderId, eventType);
        };
    }

}
