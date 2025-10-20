package com.example.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class OrderDtos {

    public record CreateOrderItemRequest(
            @NotNull Long productId,
            @NotNull @Min(1) Integer quantity
    ) {}

    public record CreateOrderRequest(
            @NotNull Long customerId,
            @NotEmpty List<CreateOrderItemRequest> items
    ) {}

    public record OrderItemResponse(
            Long productId,
            Integer quantity
    ) {}

    public record OrderResponse(
            Long id,
            Long customerId,
            String status,
            BigDecimal totalAmount,
            List<OrderItemResponse> items
    ) {}
}


