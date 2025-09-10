package com.pizzastore.order_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter@Getter
public class OrderItemDto {

    @NotNull(message = "Menu item ID is required")
    private Long menuItemId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    private String itemName;
    private BigDecimal price;

    // Constructors
    public OrderItemDto() {
    }

    public OrderItemDto(Long menuItemId, Integer quantity, String itemName, BigDecimal price) {
        this.menuItemId = menuItemId;
        this.quantity = quantity;
        this.itemName = itemName;
        this.price = price;
    }

}
