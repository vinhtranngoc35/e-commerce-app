package com.example.product.dto;

public record ProductAvailabilityResponse (long productId, boolean available, Integer availableQty){
}
