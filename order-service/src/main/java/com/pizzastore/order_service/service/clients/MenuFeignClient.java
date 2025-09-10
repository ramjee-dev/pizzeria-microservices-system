package com.pizzastore.order_service.service.clients;

import com.pizzastore.order_service.dto.MenuItemResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "MENU-SERVICE")
public interface MenuFeignClient {

    @GetMapping("/api/menu/items/{itemId}")
    MenuItemResponse getMenuItem(@PathVariable Long itemId);

}
