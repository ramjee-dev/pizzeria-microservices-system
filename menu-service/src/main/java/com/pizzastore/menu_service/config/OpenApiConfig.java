package com.pizzastore.menu_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pizzeria Menu Service API")
                        .description("REST API for managing pizzeria menu items and categories")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Pizzeria Development Team")
                                .email("support@pizzeria.com")));
    }
}
