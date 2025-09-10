package com.pizzastore.menu_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CategoryDto {

    private Long categoryId;

    @NotBlank(message = "Category name is required")
    private String name;

    private String description;

    // Constructors
    public CategoryDto() {
    }
}
