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
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {


    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final RedisTemplate<String, Integer> redisTemplate;
    private static final String CACHE_PREFIX = "product:qty:";
    private static final int NOT_FOUND_SENTINEL = -1;
    private static final Duration POSITIVE_TTL = Duration.ofMinutes(10);
    private static final Duration NEGATIVE_TTL = Duration.ofSeconds(45);

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
        // Step 1: Extract product IDs and deduplicate while keeping stable iteration order
        List<Long> productIds = requests.stream()
                .map(ProductQuantityCheckRequest::productId)
                .toList();
        Set<Long> uniqueIds = new LinkedHashSet<>(productIds);

        // Step 2: Try to get quantities from Redis cache first (batched)
        Map<Long, Integer> cacheData = new HashMap<>(uniqueIds.size());
        List<Long> missingIds = new ArrayList<>();
        try {
            cacheData.putAll(getQuantitiesFromCache(new ArrayList<>(uniqueIds)));

            // Step 3: Identify products that are not in cache
            for (Long id : uniqueIds) {
                if (!cacheData.containsKey(id)) {
                    missingIds.add(id);
                }
            }
        } catch (RuntimeException ex) {
            // Redis problem: proceed without cache
            missingIds.clear();
            missingIds.addAll(uniqueIds);
            cacheData.clear();
        }

        // Step 4: Fetch missing data from database and update cache with TTLs and sentinel
        if (!missingIds.isEmpty()) {
            List<ProductQuantityCheckRequest> dbDataList = productRepository.findQuantitiesByIds(missingIds);

            // Add database data to our cache map
            dbDataList.forEach(data -> cacheData.put(data.productId(), data.quantity()));

            // Determine IDs not found in DB
            Set<Long> foundIds = dbDataList.stream()
                    .map(ProductQuantityCheckRequest::productId)
                    .collect(Collectors.toSet());

            // Negative cache for not-found
            for (Long id : missingIds) {
                if (!foundIds.contains(id)) {
                    cacheData.put(id, NOT_FOUND_SENTINEL);
                }
            }

            // Best-effort cache writes with TTLs
            try {
                for (Map.Entry<Long, Integer> entry : cacheData.entrySet()) {
                    Long id = entry.getKey();
                    Integer qty = entry.getValue();
                    if (qty == null) {
                        continue;
                    }
                    if (Objects.equals(qty, NOT_FOUND_SENTINEL)) {
                        redisTemplate.opsForValue().set(CACHE_PREFIX + id, qty, NEGATIVE_TTL.toSeconds(), TimeUnit.SECONDS);
                    } else {
                        redisTemplate.opsForValue().set(CACHE_PREFIX + id, qty, POSITIVE_TTL.toSeconds(), TimeUnit.SECONDS);
                    }
                }
            } catch (RuntimeException ex) {
                // Ignore cache write errors
            }
        }

        // Step 5: Build response with availability check, keeping original request order
        return requests.stream()
                .map(req -> {
                    Integer cached = cacheData.get(req.productId());
                    int availableQty = (cached == null || Objects.equals(cached, NOT_FOUND_SENTINEL)) ? 0 : cached;
                    boolean available = availableQty >= req.quantity();
                    return new ProductAvailabilityResponse(req.productId(), available, availableQty);
                })
                .toList();
    }

    private Map<Long, Integer> getQuantitiesFromCache(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> keys = productIds.stream()
                .map(id -> CACHE_PREFIX + id)
                .toList();

        List<Integer> values = redisTemplate.opsForValue().multiGet(keys);
        Map<Long, Integer> cacheData = new HashMap<>(productIds.size());
        if (values == null) {
            return cacheData;
        }
        for (int i = 0; i < productIds.size(); i++) {
            Integer value = values.get(i);
            if (value != null) {
                cacheData.put(productIds.get(i), value);
            }
        }
        return cacheData;
    }
}