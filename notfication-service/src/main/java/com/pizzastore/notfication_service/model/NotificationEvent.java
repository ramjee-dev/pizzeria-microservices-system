package com.pizzastore.notfication_service.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter@Getter
public class NotificationEvent {

    private String eventId;
    private String eventType;
    private String userId;
    private String orderId;
    private String message;
    private String status;
    private String channel; // EMAIL, SMS, PUSH, WEBSOCKET

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    // Constructors
    public NotificationEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public NotificationEvent(String eventType, String userId, String message, String channel) {
        this();
        this.eventType = eventType;
        this.userId = userId;
        this.message = message;
        this.channel = channel;
        this.status = "PENDING";
    }
}