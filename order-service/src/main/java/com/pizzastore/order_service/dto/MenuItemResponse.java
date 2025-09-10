package com.pizzastore.order_service.dto;

import lombok.Getter;
import lombok.Setter;

@Setter@Getter
public class MenuItemResponse {

    private Long itemId;
    private String name;
    private Double price;
    private Boolean available;
    private String categoryName;

    // Default constructor
    public MenuItemResponse() {}
}
