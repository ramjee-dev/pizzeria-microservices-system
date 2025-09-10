package com.pizzastore.user_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Setter
@Getter
public class JwtConfig {

        private String secret = "pizzeria_jwt_secret_key_2023";
        private long expiration = 86400000; // 24 hours

    }
