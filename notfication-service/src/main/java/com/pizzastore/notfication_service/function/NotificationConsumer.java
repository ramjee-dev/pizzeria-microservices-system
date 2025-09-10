package com.pizzastore.notfication_service.function;

import com.pizzastore.notfication_service.model.NotificationEvent;
import com.pizzastore.notfication_service.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
public class NotificationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);

    @Autowired
    private NotificationService notificationService;

    @Bean
    public Consumer<Message<String>> processOrderEvents() {
        return message -> {
            try {
                String orderEvent = message.getPayload();
                String eventType = (String) message.getHeaders().get("eventType");
                String orderId = (String) message.getHeaders().get("orderId");
                String userId = (String) message.getHeaders().get("userId");

                logger.info("Processing order event: {} for order: {}", eventType, orderId);

                NotificationEvent notification = createOrderNotification(eventType, orderId, userId, orderEvent);
                notificationService.processNotification(notification);

                logger.info("Order notification processed successfully: {}", notification);

            } catch (Exception e) {
                logger.error("Error processing order event: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to process order event", e);
            }
        };
    }

    @Bean
    public Consumer<NotificationEvent> processNotificationRequests() {
        return notificationEvent -> {
            try {
                logger.info("Processing notification request: {}", notificationEvent);

                notificationService.processNotification(notificationEvent);

                logger.info("Notification processed successfully: {}", notificationEvent.getEventId());

            } catch (Exception e) {
                logger.error("Error processing notification request: {}", e.getMessage(), e);
                notificationEvent.setStatus("FAILED");
                throw new RuntimeException("Failed to process notification", e);
            }
        };
    }

    private NotificationEvent createOrderNotification(String eventType, String orderId, String userId, String orderEvent) {
        String message = generateOrderMessage(eventType, orderId);
        NotificationEvent notification = new NotificationEvent(eventType, userId, message, "EMAIL");
        notification.setOrderId(orderId);
        notification.setEventId("ORD-" + orderId + "-" + System.currentTimeMillis());
        return notification;
    }

    private String generateOrderMessage(String eventType, String orderId) {
        return switch (eventType) {
            case "ORDER_CREATED" -> String.format("🍕 Your pizza order #%s has been received and is being prepared!", orderId);
            case "ORDER_CONFIRMED" -> String.format("✅ Your order #%s has been confirmed and is now being prepared.", orderId);
            case "ORDER_PREPARING" -> String.format("👨‍🍳 Great news! Your order #%s is now being prepared by our chef.", orderId);
            case "ORDER_READY" -> String.format("🎉 Your order #%s is ready for pickup/delivery!", orderId);
            case "ORDER_DELIVERED" -> String.format("📦 Your order #%s has been successfully delivered. Enjoy your meal!", orderId);
            case "ORDER_CANCELLED" -> String.format("❌ Your order #%s has been cancelled. Refund will be processed shortly.", orderId);
            default -> String.format("📱 Update for your order #%s: %s", orderId, eventType);
        };
    }
}
