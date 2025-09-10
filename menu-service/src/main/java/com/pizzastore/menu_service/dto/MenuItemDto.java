package com.pizzastore.menu_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter@Getter
public class MenuItemDto {

    private Long itemId;

    @NotBlank(message = "Item name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private String categoryName;

    private String imageUrl;

    private Boolean available = true;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Constructors
    public MenuItemDto() {
    }

}