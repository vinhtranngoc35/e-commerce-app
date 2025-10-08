package com.example.product.dto.response;

import lombok.Builder;

@Builder
public record ProductResponse(
        Long id,
        String name,
        Double price,
        Integer quantity
) {}
