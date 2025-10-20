package com.example.payment.events;

import java.util.List;

public class OrderEvents {
    public record OrderItemEvent(Long productId, Integer quantity) {}
    public record OrderCreatedEvent(Long orderId, Long customerId, List<OrderItemEvent> items) {}
    public record OrderRejectedEvent(Long orderId, Long customerId, String reason, List<OrderItemEvent> items) {}
    public record PaymentCompletedEvent(Long orderId, Long customerId) {}
    public record PaymentFailedEvent(Long orderId, Long customerId, String reason) {}
}


