package com.example.product.kafka.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCacheSyncConsumer {

    private final RedisTemplate<String, Integer> redisTemplate;
    private static final String CACHE_PREFIX = "product:qty:";

    @KafkaListener(topics = "dbserver1.public.product", groupId = "product-cache-sync")
    public void handleProductChange(String message) {
        log.info("Received CDC message: {}", message);
        try {
            JsonNode node = new ObjectMapper().readTree(message);
            JsonNode after = node.path("payload").path("after");
            JsonNode op = node.path("payload").path("op");

            if (after.isMissingNode()) {
                return;
            }

            Long id = after.path("id").asLong();
            Integer quantity = after.path("quantity").isMissingNode() ? null : after.path("quantity").asInt();

            // chỉ cập nhật khi là update hoặc insert
            String operation = node.path("payload").path("op").asText();
            if ("u".equals(operation) || "c".equals(operation)) {
                redisTemplate.opsForValue().set(CACHE_PREFIX + id, quantity);
                log.info("Cache updated from CDC: product {} -> quantity {}", id, quantity);
            }

        } catch (Exception e) {
            log.error("Error processing CDC message: {}", message, e);
        }
    }
}
