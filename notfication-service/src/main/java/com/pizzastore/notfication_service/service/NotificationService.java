package com.pizzastore.notfication_service.service;

import com.pizzastore.notfication_service.model.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private StreamBridge streamBridge;

    public void processNotification(NotificationEvent notification) {
        try {
            logger.info("Processing notification: {}", notification);

            // Process notification asynchronously
            CompletableFuture.runAsync(() -> {
                switch (notification.getChannel().toUpperCase()) {
                    case "EMAIL" -> sendEmailNotification(notification);
                    case "SMS" -> sendSmsNotification(notification);
                    case "PUSH" -> sendPushNotification(notification);
                    case "WEBSOCKET" -> sendWebSocketNotification(notification);
                    default -> logger.warn("Unknown notification channel: {}", notification.getChannel());
                }

                // Update notification status
                notification.setStatus("SENT");

                // Publish notification sent event
                publishNotificationSent(notification);
            });

        } catch (Exception e) {
            logger.error("Error processing notification: {}", e.getMessage(), e);
            notification.setStatus("FAILED");
            throw new RuntimeException("Notification processing failed", e);
        }
    }

    private void sendEmailNotification(NotificationEvent notification) {
        try {  // MOCK EMAIL SENDING FOR DEVELOPMENT
            logger.info("📧 EMAIL SIMULATION - TO: user-{}", notification.getUserId());
            logger.info("📧 SUBJECT: Pizzeria Order Update - {}", notification.getEventType());
            logger.info("📧 MESSAGE: {}", notification.getMessage());
            logger.info("📧 Email would be sent successfully!");
            /*
            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo("customer@pizzeria.com"); // In real app, get from user profile
                message.setSubject("Pizzeria Order Update - " + notification.getEventType());
                message.setText(notification.getMessage());
                message.setFrom("noreply@pizzeria.com");

                mailSender.send(message);
                logger.info("Email sent successfully for notification: {}", notification.getEventId());
            } else {
                logger.info("EMAIL SIMULATION: {}", notification.getMessage());
            }*/
        } catch (Exception e) {
            logger.error("Failed to send email notification: {}", e.getMessage(), e);
            throw new RuntimeException("Email sending failed", e);
        }
    }

    private void sendSmsNotification(NotificationEvent notification) {
        // SMS integration (Twilio, AWS SNS, etc.)
        logger.info("SMS SIMULATION: Sending SMS to user {}: {}",
                notification.getUserId(), notification.getMessage());

        // Simulate SMS delay
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void sendPushNotification(NotificationEvent notification) {
        // Push notification integration (Firebase, etc.)
        logger.info("PUSH SIMULATION: Sending push notification to user {}: {}",
                notification.getUserId(), notification.getMessage());
    }

    private void sendWebSocketNotification(NotificationEvent notification) {
        // WebSocket real-time notification
        logger.info("WEBSOCKET SIMULATION: Broadcasting to user {}: {}",
                notification.getUserId(), notification.getMessage());

        // In real implementation, use WebSocket to push to connected clients
    }

    private void publishNotificationSent(NotificationEvent notification) {
        try {
            streamBridge.send("sendNotifications-out-0", notification);
            logger.debug("Published notification sent event: {}", notification.getEventId());
        } catch (Exception e) {
            logger.warn("Failed to publish notification sent event: {}", e.getMessage());
        }
    }

    // Method to send notification programmatically
    public void sendNotification(String eventType, String userId, String message, String channel) {
        NotificationEvent notification = new NotificationEvent(eventType, userId, message, channel);
        notification.setEventId("API-" + System.currentTimeMillis());
        processNotification(notification);
    }
}
