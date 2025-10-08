package com.example.product.repository;

import com.example.product.dto.request.ProductQuantityCheckRequest;
import com.example.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = "SELECT new com.example.product.dto.request.ProductQuantityCheckRequest(id, quantity) FROM Product WHERE id in :missingIds")
    List<ProductQuantityCheckRequest> findQuantitiesByIds(List<Long> missingIds);
}
