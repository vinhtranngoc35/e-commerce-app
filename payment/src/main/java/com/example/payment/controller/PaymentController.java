package com.example.payment.controller;

import com.example.payment.service.PaymentAccountService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentAccountService paymentAccountService;

    public PaymentController(PaymentAccountService paymentAccountService) {
        this.paymentAccountService = paymentAccountService;
    }

    @PostMapping("/precheck")
    public ResponseEntity<Boolean> precheck(@RequestParam @NotNull Long customerId,
                                            @RequestParam @NotNull BigDecimal amount) {
        return ResponseEntity.ok(paymentAccountService.hasSufficientBalance(customerId, amount));
    }
}


