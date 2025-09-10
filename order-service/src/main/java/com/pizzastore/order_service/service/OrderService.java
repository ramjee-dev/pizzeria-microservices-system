package com.pizzastore.order_service.service;

import com.pizzastore.order_service.dto.MenuItemResponse;
import com.pizzastore.order_service.dto.OrderDto;
import com.pizzastore.order_service.dto.OrderItemDto;
import com.pizzastore.order_service.dto.OrderRequestDto;
import com.pizzastore.order_service.entity.DeliveryMode;
import com.pizzastore.order_service.entity.Order;
import com.pizzastore.order_service.entity.OrderItem;
import com.pizzastore.order_service.entity.OrderStatus;
import com.pizzastore.order_service.repository.OrderRepository;
import com.pizzastore.order_service.service.clients.MenuFeignClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private MenuFeignClient menuFeignClient;
    @Autowired
    private StreamBridge streamBridge;

    // Create new order
    public OrderDto createOrder(OrderRequestDto orderRequest) {
        logger.info("Creating order for user ID: {}", orderRequest.getUserId());

        // Fixed validation in OrderService.java
        for (OrderItemDto item : orderRequest.getItems()) {
            try {
                MenuItemResponse menuItem = menuFeignClient.getMenuItem(item.getMenuItemId());
                if (menuItem == null) {
                    throw new RuntimeException("Menu item not found with ID: " + item.getMenuItemId());
                }
                if (!menuItem.getAvailable()) {
                    throw new RuntimeException("Menu item not available: " + menuItem.getName());
                }
                // Update item details from menu service
                item.setItemName(menuItem.getName());
                item.setPrice(BigDecimal.valueOf(menuItem.getPrice()));
            } catch (Exception e) {
                logger.error("Error validating menu item {}: {}", item.getMenuItemId(), e.getMessage());
                throw new RuntimeException("Invalid menu item ID: " + item.getMenuItemId());
            }
        }

        Order order = new Order();
        order.setUserId(orderRequest.getUserId());
        order.setDeliveryMode(DeliveryMode.valueOf(orderRequest.getDeliveryMode().toUpperCase()));
        order.setDeliveryAddress(orderRequest.getDeliveryAddress());
        order.setStatus(OrderStatus.PENDING);

        // Calculate total amount and create order items
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = orderRequest.getItems().stream()
                .map(itemDto -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setMenuItemId(itemDto.getMenuItemId());
                    orderItem.setItemName(itemDto.getItemName());
                    orderItem.setQuantity(itemDto.getQuantity());
                    orderItem.setPrice(itemDto.getPrice());
                    orderItem.setTotalPrice(itemDto.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));

                    return orderItem;
                })
                .collect(Collectors.toList());

        // Calculate total amount
        totalAmount = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(totalAmount);
        order.setOrderItems(orderItems);

        Order savedOrder = orderRepository.save(order);


        logger.info("Order created successfully with ID: {}", savedOrder.getOrderId());

        // 🚀 PUBLISH ORDER CREATED EVENT
        publishOrderEvent(savedOrder.getOrderId(), savedOrder.getUserId(), "ORDER_CREATED");

        return convertToOrderDto(savedOrder);
    }



    // Get order by ID
    public OrderDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        return convertToOrderDto(order);
    }

    // Get user orders
    public List<OrderDto> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByOrderDateDesc(userId);
        return orders.stream()
                .map(this::convertToOrderDto)
                .collect(Collectors.toList());
    }

    // Get all orders (Admin)
    public List<OrderDto> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::convertToOrderDto)
                .collect(Collectors.toList());
    }

    // Update order status (Admin)
    public OrderDto updateOrderStatus(Long orderId, String status) {
        logger.info("Updating order {} status to {}", orderId, status);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        order.setStatus(OrderStatus.valueOf(status.toUpperCase()));
        Order savedOrder = orderRepository.save(order);

        logger.info("Order status updated successfully");

        // 🚀 PUBLISH ORDER STATUS UPDATE EVENT
        publishOrderEvent(savedOrder.getOrderId(), savedOrder.getUserId(), "ORDER_" + status.toUpperCase());

        return convertToOrderDto(savedOrder);
    }

    // Get order statistics
    public Map<String, Object> getOrderStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalOrders", orderRepository.count());
        stats.put("pendingOrders", orderRepository.countByStatus(OrderStatus.PENDING));
        stats.put("confirmedOrders", orderRepository.countByStatus(OrderStatus.CONFIRMED));
        stats.put("deliveredOrders", orderRepository.countByStatus(OrderStatus.DELIVERED));
        stats.put("cancelledOrders", orderRepository.countByStatus(OrderStatus.CANCELLED));

        return stats;
    }

    // 🔥 NEW: Publish Order Event Method
    public void publishOrderEvent(Long orderId, Long userId, String eventType) {
        try {
            // Create event payload
            Map<String, Object> orderEventPayload = new HashMap<>();
            orderEventPayload.put("orderId", orderId.toString());
            orderEventPayload.put("userId", userId.toString());
            orderEventPayload.put("eventType", eventType);
            orderEventPayload.put("timestamp", LocalDateTime.now().toString());
            orderEventPayload.put("serviceName", "ORDER-SERVICE");

            // Build message with headers
            Message<Map<String, Object>> message = MessageBuilder
                    .withPayload(orderEventPayload)
                    .setHeader("eventType", eventType)
                    .setHeader("orderId", orderId.toString())
                    .setHeader("userId", userId.toString())
                    .setHeader("source", "ORDER-SERVICE")
                    .build();

            // Send to order.events destination
            boolean sent = streamBridge.send("order-events-out-0", message);

            if (sent) {
                logger.info("✅ Published order event: {} for order: {} to queue: order.events",
                        eventType, orderId);
            } else {
                logger.warn("❌ Failed to publish order event: {} for order: {}", eventType, orderId);
            }

        } catch (Exception e) {
            logger.error("💥 Error publishing order event: {} for order: {} - {}",
                    eventType, orderId, e.getMessage(), e);
            // Don't throw exception to avoid breaking the main order flow
        }
    }

    // 🔥 NEW: Direct Notification Publishing (Optional)
    public void publishNotificationRequest(Long orderId, Long userId, String message, String channel) {
        try {
            Map<String, Object> notificationPayload = new HashMap<>();
            notificationPayload.put("eventType", "DIRECT_NOTIFICATION");
            notificationPayload.put("orderId", orderId.toString());
            notificationPayload.put("userId", userId.toString());
            notificationPayload.put("message", message);
            notificationPayload.put("channel", channel);
            notificationPayload.put("timestamp", LocalDateTime.now().toString());

            boolean sent = streamBridge.send("notification-requests-out-0", notificationPayload);

            if (sent) {
                logger.info("✅ Published notification request for order: {}", orderId);
            } else {
                logger.warn("❌ Failed to publish notification request for order: {}", orderId);
            }

        } catch (Exception e) {
            logger.error("💥 Error publishing notification request: {}", e.getMessage(), e);
        }
    }


    // Helper method to convert Order to OrderDto
    private OrderDto convertToOrderDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setOrderId(order.getOrderId());
        dto.setUserId(order.getUserId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().name());
        dto.setDeliveryMode(order.getDeliveryMode().name());
        dto.setDeliveryAddress(order.getDeliveryAddress());
        dto.setOrderDate(order.getOrderDate());

        if (order.getOrderItems() != null) {
            List<OrderItemDto> itemDtos = order.getOrderItems().stream()
                    .map(item -> new OrderItemDto(
                            item.getMenuItemId(),
                            item.getQuantity(),
                            item.getItemName(),
                            item.getPrice()
                    ))
                    .collect(Collectors.toList());
            dto.setItems(itemDtos);
        }

        return dto;
    }
}
