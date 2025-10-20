package com.example.payment.consumer;

import com.example.payment.config.PaymentTopicsProperties;
import com.example.payment.events.OrderEvents.OrderCreatedEvent;
import com.example.payment.events.OrderEvents.PaymentCompletedEvent;
import com.example.payment.events.OrderEvents.PaymentFailedEvent;
import com.example.payment.service.PaymentAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentConsumer {

    private final PaymentTopicsProperties topics;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PaymentAccountService paymentAccountService;

    @KafkaListener(topics = "#{@paymentTopicsProperties.created}")
    public void onOrderCreated(@Payload OrderCreatedEvent event) {
        log.info("Processing payment for order={} customer={}", event.orderId(), event.customerId());
        // Simulate lookup of amount by order or carry amount in event; here we assume a fixed amount for demo
        // In production, include BigDecimal amount in OrderCreatedEvent and use it directly
        boolean success = paymentAccountService.deduct(event.customerId(), java.math.BigDecimal.valueOf(1));
        if (success) {
            kafkaTemplate.send(topics.getPaymentCompleted(), new PaymentCompletedEvent(event.orderId(), event.customerId()));
        } else {
            kafkaTemplate.send(topics.getPaymentFailed(), new PaymentFailedEvent(event.orderId(), event.customerId(), "CARD_DECLINED"));
        }
    }
}


