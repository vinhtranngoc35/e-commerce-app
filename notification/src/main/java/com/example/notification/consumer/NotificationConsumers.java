package com.example.notification.consumer;

import com.example.notification.events.OrderEvents.OrderCreatedEvent;
import com.example.notification.events.OrderEvents.OrderRejectedEvent;
import com.example.notification.events.OrderEvents.PaymentCompletedEvent;
import com.example.notification.events.OrderEvents.PaymentFailedEvent;
import com.example.notification.config.NotificationTopicsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumers {

    private final NotificationTopicsProperties topics;

    @KafkaListener(topics = "#{@notificationTopicsProperties.created}")
    public void onOrderCreated(@Payload OrderCreatedEvent event) {
        log.info("Notify ORDER_CREATED: orderId={}, customerId={}, items={}", event.orderId(), event.customerId(), event.items());
    }

    @KafkaListener(topics = "#{@notificationTopicsProperties.rejected}")
    public void onOrderRejected(@Payload OrderRejectedEvent event) {
        log.info("Notify ORDER_REJECTED: orderId={}, customerId={}, reason={}, items={}", event.orderId(), event.customerId(), event.reason(), event.items());
    }

    @KafkaListener(topics = "#{@notificationTopicsProperties.paymentCompleted}")
    public void onPaymentCompleted(@Payload PaymentCompletedEvent event) {
        log.info("Notify PAYMENT_COMPLETED: orderId={}, customerId={}", event.orderId(), event.customerId());
    }

    @KafkaListener(topics = "#{@notificationTopicsProperties.paymentFailed}")
    public void onPaymentFailed(@Payload PaymentFailedEvent event) {
        log.info("Notify PAYMENT_FAILED: orderId={}, customerId={}, reason={}", event.orderId(), event.customerId(), event.reason());
    }
}


