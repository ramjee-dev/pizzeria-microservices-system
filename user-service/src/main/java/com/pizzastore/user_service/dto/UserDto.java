package com.pizzastore.user_service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDto {

    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String role;
    private LocalDateTime createdAt;
    private Boolean active;
}
