package com.example.payment.repository;

import com.example.payment.entity.PaymentAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentAccountRepository extends JpaRepository<PaymentAccount, Long> {
    Optional<PaymentAccount> findByCustomerId(Long customerId);
}


