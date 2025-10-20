package com.example.payment.service;

import com.example.payment.entity.PaymentAccount;
import com.example.payment.repository.PaymentAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentAccountService {

    private final PaymentAccountRepository repository;

    @Transactional(readOnly = true)
    public boolean hasSufficientBalance(Long customerId, BigDecimal amount) {
        return repository.findByCustomerId(customerId)
                .map(acc -> acc.getBalance().compareTo(amount) >= 0)
                .orElse(false);
    }

    @Transactional
    public boolean deduct(Long customerId, BigDecimal amount) {
        PaymentAccount account = repository.findByCustomerId(customerId)
                .orElse(null);
        if (account == null) {
            return false;
        }
        if (account.getBalance().compareTo(amount) < 0) {
            return false;
        }
        account.setBalance(account.getBalance().subtract(amount));
        repository.save(account);
        return true;
    }
}


