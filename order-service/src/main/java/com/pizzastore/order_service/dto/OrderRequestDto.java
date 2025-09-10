package com.pizzastore.order_service.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter@Getter
public class OrderRequestDto {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItemDto> items;

    private String deliveryMode = "DELIVERY";
    private String deliveryAddress;

    // Constructors
    public OrderRequestDto() {
    }

}
