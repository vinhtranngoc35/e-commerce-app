package com.example.product.service;

import com.example.product.dto.ProductAvailabilityResponse;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {


    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final RedisTemplate<String, Integer> redisTemplate;
    private static final String CACHE_PREFIX = "product:qty:";

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
    public List<ProductAvailabilityResponse> checkQuantities(List<ProductQuantityCheckRequest> requests) {
        // Step 1: Extract product IDs from requests
        List<Long> productIds = requests.stream()
                .map(ProductQuantityCheckRequest::productId)
                .toList();

        // Step 2: Try to get quantities from Redis cache first
        Map<Long, Integer> cacheData = getQuantitiesFromCache(productIds);

        // Step 3: Identify products that are not in cache
        List<Long> missingIds = productIds.stream()
                .filter(id -> !cacheData.containsKey(id))
                .toList();

        // Step 4: Fetch missing data from database and update cache
        if (!missingIds.isEmpty()) {
            List<ProductQuantityCheckRequest> dbDataList = productRepository.findQuantitiesByIds(missingIds);

            // Cache the actual data from database
            dbDataList.forEach(data ->
                    redisTemplate.opsForValue().set(CACHE_PREFIX + data.productId(), data.quantity())
            );

            // Add database data to our cache map
            dbDataList.forEach(data -> cacheData.put(data.productId(), data.quantity()));

            // For IDs that don't exist in DB, cache null to avoid repeated DB queries
            Set<Long> foundIds = dbDataList.stream()
                    .map(ProductQuantityCheckRequest::productId)
                    .collect(Collectors.toSet());

            missingIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .forEach(id -> {
                        redisTemplate.opsForValue().set(CACHE_PREFIX + id, null);
                        cacheData.put(id, null);
                    });
        }

        // Step 5: Build response with availability check
        return requests.stream()
                .map(req -> {
                    Integer availableQty = cacheData.get(req.productId());
                    boolean available = availableQty != null && availableQty >= req.quantity();
                    return new ProductAvailabilityResponse(req.productId(), available, availableQty);
                })
                .toList();
    }

    private Map<Long, Integer> getQuantitiesFromCache(List<Long> productIds) {
        Map<Long, Integer> cacheData = new HashMap<>();
        productIds.forEach(id -> {
            Integer cachedQty = redisTemplate.opsForValue().get(CACHE_PREFIX + id);
            if (cachedQty != null) {
                cacheData.put(id, cachedQty);
            }
        });
        return cacheData;
    }
}