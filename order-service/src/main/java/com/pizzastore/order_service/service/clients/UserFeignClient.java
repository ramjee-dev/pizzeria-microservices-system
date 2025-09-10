package com.pizzastore.order_service.service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "USER-SERVICE")
public interface UserFeignClient {

    @GetMapping("/api/users/validate-token")
    Boolean validateToken(@RequestParam String token);

    @GetMapping("/api/users/username-from-token")
    String getUsernameFromToken(@RequestParam String token);
}
