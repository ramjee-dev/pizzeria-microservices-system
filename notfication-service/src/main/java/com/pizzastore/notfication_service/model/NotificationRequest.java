package com.pizzastore.notfication_service.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter@Getter@ToString
public class NotificationRequest {

    private String eventType;
    private String userId;
    private String message;
    private String channel = "EMAIL";
}
