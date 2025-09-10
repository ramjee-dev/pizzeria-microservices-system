package com.pizzastore.user_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Setter@Getter@ToString
public class LoginResponseDto {

    @JsonProperty("token")
    private String token;

    @JsonProperty("tokenType")
    private String tokenType = "Bearer";

    @JsonProperty("username")
    private String username;

    @JsonProperty("email")
    private String email;

    @JsonProperty("role")
    private String role;

    @JsonProperty("userId")
    private Long userId;

    // Default constructor
    public LoginResponseDto() {
    }

    // Constructor with all parameters
    public LoginResponseDto(String token, String username, String email, String role, Long userId) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.role = role;
        this.userId = userId;
        this.tokenType = "Bearer"; // Set default token type
    }

}
