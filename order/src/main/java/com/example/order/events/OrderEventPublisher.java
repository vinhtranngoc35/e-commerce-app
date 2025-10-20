package com.example.order.events;

import com.example.order.config.OrderTopicsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrderTopicsProperties topics;

    public void publishCreated(Object payload) {
        kafkaTemplate.send(topics.getCreated(), payload);
    }

    public void publishRejected(Object payload) {
        kafkaTemplate.send(topics.getRejected(), payload);
    }

    public void publishPaymentCompleted(Object payload) {
        kafkaTemplate.send(topics.getPaymentCompleted(), payload);
    }

    public void publishPaymentFailed(Object payload) {
        kafkaTemplate.send(topics.getPaymentFailed(), payload);
    }
}


