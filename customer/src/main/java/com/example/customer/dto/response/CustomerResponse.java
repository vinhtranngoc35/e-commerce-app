package com.example.customer.dto.response;

public record CustomerResponse(
        Long id,
        String firstname,
        String lastname,
        String email,
        String address
) {}
