package com.pizzastore.menu_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Setter@Getter
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;
    private Map<String, String> fieldErrors;

    // Constructors
    public ErrorResponse(int status, String error, String message, String path, LocalDateTime timestamp) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = timestamp;
    }

    public ErrorResponse(int status, String error, String message, String path,
                         LocalDateTime timestamp, Map<String, String> fieldErrors) {
        this(status, error, message, path, timestamp);
        this.fieldErrors = fieldErrors;
    }
}
