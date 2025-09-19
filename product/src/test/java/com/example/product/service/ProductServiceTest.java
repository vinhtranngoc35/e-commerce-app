package com.example.product.service;

import com.example.product.dto.request.ProductCreateRequest;
import com.example.product.dto.request.ProductQuantityCheckRequest;
import com.example.product.dto.request.ProductUpdateRequest;
import com.example.product.dto.response.ProductResponse;
import com.example.product.entity.Product;
import com.example.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_shouldCreateProduct() {
        ProductCreateRequest request = new ProductCreateRequest("Test Product", 10.0, 100);
        Product product = new Product();
        ProductResponse productResponse = new ProductResponse(1L, "Test Product", 10.0, 100);

        when(modelMapper.map(request, Product.class)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(modelMapper.map(product, ProductResponse.class)).thenReturn(productResponse);

        ProductResponse result = productService.create(request);

        assertEquals(productResponse, result);
        verify(productRepository).save(product);
    }

    @Test
    void findAll_shouldReturnPageOfProductResponses() {
        Pageable pageable = PageRequest.of(0, 10);
        Product product = new Product();
        ProductResponse productResponse = new ProductResponse(1L, "Test Product", 10.0, 100);
        Page<Product> page = new PageImpl<>(Collections.singletonList(product));

        when(productRepository.findAll(pageable)).thenReturn(page);
        when(modelMapper.map(product, ProductResponse.class)).thenReturn(productResponse);

        Page<ProductResponse> result = productService.findAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(productResponse, result.getContent().get(0));
    }

    @Test
    void findById_shouldReturnProductResponseWhenFound() {
        Long id = 1L;
        Product product = new Product();
        ProductResponse productResponse = new ProductResponse(1L, "Test Product", 10.0, 100);

        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(modelMapper.map(product, ProductResponse.class)).thenReturn(productResponse);

        Optional<ProductResponse> result = productService.findById(id);

        assertTrue(result.isPresent());
        assertEquals(productResponse, result.get());
    }

    @Test
    void findById_shouldReturnEmptyOptionalWhenNotFound() {
        Long id = 1L;
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        Optional<ProductResponse> result = productService.findById(id);

        assertTrue(result.isEmpty());
    }

    @Test
    void update_shouldUpdateProductWhenFound() {
        Long id = 1L;
        ProductUpdateRequest request = new ProductUpdateRequest("Updated Product", 20.0, 200);
        Product existingProduct = new Product();
        ProductResponse productResponse = new ProductResponse(1L, "Updated Product", 20.0, 200);

        when(productRepository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(existingProduct)).thenReturn(existingProduct);
        when(modelMapper.map(existingProduct, ProductResponse.class)).thenReturn(productResponse);

        ProductResponse result = productService.update(id, request);

        assertEquals(productResponse, result);
        verify(modelMapper).map(request, existingProduct);
        verify(productRepository).save(existingProduct);
    }

    @Test
    void update_shouldThrowExceptionWhenNotFound() {
        Long id = 1L;
        ProductUpdateRequest request = new ProductUpdateRequest("Updated Product", 20.0, 200);

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.update(id, request));
    }

    @Test
    void deleteById_shouldDeleteProduct() {
        Long id = 1L;
        doNothing().when(productRepository).deleteById(id);

        productService.deleteById(id);

        verify(productRepository).deleteById(id);
    }

    @Test
    void isAvailable_shouldReturnTrueWhenAllProductsAreAvailable() {
        ProductQuantityCheckRequest item1 = new ProductQuantityCheckRequest(1L, 10);
        ProductQuantityCheckRequest item2 = new ProductQuantityCheckRequest(2L, 20);
        List<ProductQuantityCheckRequest> items = Arrays.asList(item1, item2);

        Product product1 = new Product();
        product1.setId(1L);
        product1.setStock(100);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setStock(200);

        when(productRepository.findAllById(Arrays.asList(1L, 2L))).thenReturn(Arrays.asList(product1, product2));

        assertTrue(productService.isAvailable(items));
    }

    @Test
    void isAvailable_shouldReturnFalseWhenOneProductIsNotAvailable() {
        ProductQuantityCheckRequest item1 = new ProductQuantityCheckRequest(1L, 10);
        ProductQuantityCheckRequest item2 = new ProductQuantityCheckRequest(2L, 20);
        List<ProductQuantityCheckRequest> items = Arrays.asList(item1, item2);

        Product product1 = new Product();
        product1.setId(1L);
        product1.setStock(5);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setStock(200);

        when(productRepository.findAllById(Arrays.asList(1L, 2L))).thenReturn(Arrays.asList(product1, product2));

        assertFalse(productService.isAvailable(items));
    }

    @Test
    void isAvailable_shouldReturnFalseWhenOneProductIsNotFound() {
        ProductQuantityCheckRequest item1 = new ProductQuantityCheckRequest(1L, 10);
        ProductQuantityCheckRequest item2 = new ProductQuantityCheckRequest(2L, 20);
        List<ProductQuantityCheckRequest> items = Arrays.asList(item1, item2);

        Product product1 = new Product();
        product1.setId(1L);
        product1.setStock(100);

        when(productRepository.findAllById(Arrays.asList(1L, 2L))).thenReturn(Collections.singletonList(product1));

        assertFalse(productService.isAvailable(items));
    }
}
