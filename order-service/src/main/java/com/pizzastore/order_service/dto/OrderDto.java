package com.pizzastore.order_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Setter@Getter
public class OrderDto {

    private Long orderId;
    private Long userId;
    private BigDecimal totalAmount;
    private String status;
    private String deliveryMode;
    private String deliveryAddress;
    private LocalDateTime orderDate;
    private List<OrderItemDto> items;

    // Constructors
    public OrderDto() {
    }

}