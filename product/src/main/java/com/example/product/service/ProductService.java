package com.example.product.service;

import com.example.product.dto.request.ProductCreateRequest;
import com.example.product.dto.request.ProductQuantityCheckRequest;
import com.example.product.dto.request.ProductUpdateRequest;
import com.example.product.dto.response.ProductResponse;
import com.example.product.entity.Product;
import com.example.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {


    private final ProductRepository productRepository;


    private final ModelMapper modelMapper;

    @Override
    public ProductResponse create(ProductCreateRequest request) {
        Product product = modelMapper.map(request, Product.class);
        Product savedProduct = productRepository.save(product);
        return modelMapper.map(savedProduct, ProductResponse.class);
    }

    @Override
    public Page<ProductResponse> findAll(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(product -> modelMapper.map(product, ProductResponse.class));
    }

    @Override
    public Optional<ProductResponse> findById(Long id) {
        return productRepository.findById(id)
                .map(product -> modelMapper.map(product, ProductResponse.class));
    }

    @Override
    public ProductResponse update(Long id, ProductUpdateRequest request) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id " + id));

        modelMapper.map(request, existingProduct);
        Product updatedProduct = productRepository.save(existingProduct);
        return modelMapper.map(updatedProduct, ProductResponse.class);
    }

    @Override
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAvailable(List<ProductQuantityCheckRequest> items) {
        List<Long> productIds = items.stream()
                .map(ProductQuantityCheckRequest::productId)
                .collect(Collectors.toList());

        List<Product> products = productRepository.findAllById(productIds);

        Map<Long, Integer> stockMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Product::getStock));

        for (ProductQuantityCheckRequest item : items) {
            Integer currentStock = stockMap.get(item.productId());
            if (currentStock == null || currentStock < item.quantity()) {
                return false;
            }
        }

        return true;
    }
}