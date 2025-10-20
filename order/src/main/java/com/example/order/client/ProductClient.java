package com.example.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "product", path = "/api/products")
public interface ProductClient {

    record QuantityCheckItem(Long productId, Integer quantity) {}
    record AvailabilityResponse(long productId, boolean available, Integer availableQty) {}

    @PostMapping("/check-availability")
    List<AvailabilityResponse> checkAvailability(@RequestBody List<QuantityCheckItem> items);
}


