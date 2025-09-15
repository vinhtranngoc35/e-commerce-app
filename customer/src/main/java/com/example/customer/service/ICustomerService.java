package com.example.customer.service;


import com.example.customer.dto.request.CustomerCreateRequest;
import com.example.customer.dto.request.CustomerUpdateRequest;
import com.example.customer.dto.response.CustomerResponse;
import com.example.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ICustomerService {
    CustomerResponse create(CustomerCreateRequest request);
    Page<CustomerResponse> findAll(Pageable pageable);
    Optional<CustomerResponse> findById(Long id);
    CustomerResponse update(Long id, CustomerUpdateRequest request);
    void deleteById(Long id);
}