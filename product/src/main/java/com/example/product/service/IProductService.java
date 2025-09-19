package com.example.product.service;

import com.example.product.dto.request.ProductCreateRequest;
import com.example.product.dto.request.ProductQuantityCheckRequest;
import com.example.product.dto.response.ProductResponse;
import com.example.product.dto.request.ProductUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface IProductService {
    ProductResponse create(ProductCreateRequest request);
    Page<ProductResponse> findAll(Pageable pageable);
    Optional<ProductResponse> findById(Long id);
    ProductResponse update(Long id, ProductUpdateRequest request);
    void deleteById(Long id);
    boolean isAvailable(List<ProductQuantityCheckRequest> items);
}