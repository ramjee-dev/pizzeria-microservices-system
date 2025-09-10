package com.pizzastore.gateway_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@SpringBootApplication
public class GatewayServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayServerApplication.class, args);
	}

	@Bean
	public RouteLocator pizzeriaRouteConfig(RouteLocatorBuilder routeLocatorBuilder) {
		return routeLocatorBuilder.routes()
				.route(p -> p
						.path("/pizzeria/users/**")
						.filters(f -> f.rewritePath("/pizzeria/users/(?<segment>.*)", "/${segment}")
								.addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
								.addResponseHeader("X-Service", "USER-SERVICE"))
						.uri("lb://USER-SERVICE"))

				.route(p -> p
						.path("/pizzeria/menu/**")
						.filters(f -> f.rewritePath("/pizzeria/menu/(?<segment>.*)", "/${segment}")
								.addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
								.addResponseHeader("X-Service", "MENU-SERVICE"))
						.uri("lb://MENU-SERVICE"))

				.route(p -> p
						.path("/pizzeria/orders/**")
						.filters(f -> f.rewritePath("/pizzeria/orders/(?<segment>.*)", "/${segment}")
								.addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
								.addResponseHeader("X-Service", "ORDER-SERVICE"))
						.uri("lb://ORDER-SERVICE"))

				.build();
	}

	// ApiGatewayApplication.java - Add this SECOND bean
	@Bean
	public RouteLocator swaggerRouteConfig(RouteLocatorBuilder routeLocatorBuilder) {
		return routeLocatorBuilder.routes()
				// Route for User Service API docs
				.route("user-service-docs", p -> p
						.path("/pizzeria/users/v3/api-docs")
						.filters(f -> f.rewritePath("/pizzeria/users/v3/api-docs", "/v3/api-docs")
								.addResponseHeader("Access-Control-Allow-Origin", "*"))
						.uri("lb://USER-SERVICE"))

				// Route for Menu Service API docs
				.route("menu-service-docs", p -> p
						.path("/pizzeria/menu/v3/api-docs")
						.filters(f -> f.rewritePath("/pizzeria/menu/v3/api-docs", "/v3/api-docs")
								.addResponseHeader("Access-Control-Allow-Origin", "*"))
						.uri("lb://MENU-SERVICE"))

				// Route for Order Service API docs
				.route("order-service-docs", p -> p
						.path("/pizzeria/orders/v3/api-docs")
						.filters(f -> f.rewritePath("/pizzeria/orders/v3/api-docs", "/v3/api-docs")
								.addResponseHeader("Access-Control-Allow-Origin", "*"))
						.uri("lb://ORDER-SERVICE"))

				.build();
	}

// Keep your existing pizzeriaRouteConfig bean unchanged!


}
