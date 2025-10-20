package com.example.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "payment", path = "/api/payments")
public interface PaymentClient {

    @PostMapping("/precheck")
    Boolean precheck(@RequestParam("customerId") Long customerId,
                     @RequestParam("amount") BigDecimal amount);
}


