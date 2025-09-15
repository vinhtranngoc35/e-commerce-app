package com.example.customer.service;

import com.example.customer.dto.request.AddressCreateRequest;
import com.example.customer.dto.request.AddressUpdateRequest;
import com.example.customer.dto.request.CustomerCreateRequest;
import com.example.customer.dto.request.CustomerUpdateRequest;
import com.example.customer.dto.response.CustomerResponse;
import com.example.customer.entity.Address;
import com.example.customer.entity.Customer;
import com.example.customer.repository.ICustomerRepository;
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

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerServiceTest {

    @InjectMocks
    private CustomerService customerService;

    @Mock
    private ICustomerRepository customerRepository;

    @Mock
    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_shouldCreateCustomer() {
        AddressCreateRequest addressCreateRequest = new AddressCreateRequest("123 Main St", "Anytown", "12345");
        CustomerCreateRequest request = new CustomerCreateRequest("John", "Doe", "john.doe@example.com", addressCreateRequest);
        Customer customer = new Customer();
        Address address = new Address();
        CustomerResponse customerResponse = new CustomerResponse(1L, "John", "Doe", "john.doe@example.com", "123 Main St, Anytown, 12345");

        when(modelMapper.map(request, Customer.class)).thenReturn(customer);
        when(modelMapper.map(request.address(), Address.class)).thenReturn(address);
        when(customerRepository.save(customer)).thenReturn(customer);
        when(modelMapper.map(customer, CustomerResponse.class)).thenReturn(customerResponse);

        CustomerResponse result = customerService.create(request);

        assertEquals(customerResponse, result);
        verify(customerRepository).save(customer);
    }

    @Test
    void findAll_shouldReturnPageOfCustomerResponses() {
        Pageable pageable = PageRequest.of(0, 10);
        CustomerResponse customerResponse = new CustomerResponse(1L, "John", "Doe", "john.doe@example.com", "123 Main St, Anytown, 12345");
        Page<CustomerResponse> page = new PageImpl<>(Collections.singletonList(customerResponse));
        when(customerRepository.findAllCustomerResponse(pageable)).thenReturn(page);

        Page<CustomerResponse> result = customerService.findAll(pageable);

        assertEquals(page, result);
    }

    @Test
    void findById_shouldReturnCustomerResponseWhenFound() {
        Long id = 1L;
        CustomerResponse customerResponse = new CustomerResponse(1L, "John", "Doe", "john.doe@example.com", "123 Main St, Anytown, 12345");
        when(customerRepository.findCustomerResponseById(id)).thenReturn(Optional.of(customerResponse));

        Optional<CustomerResponse> result = customerService.findById(id);

        assertTrue(result.isPresent());
        assertEquals(customerResponse, result.get());
    }

    @Test
    void findById_shouldReturnEmptyOptionalWhenNotFound() {
        Long id = 1L;
        when(customerRepository.findCustomerResponseById(id)).thenReturn(Optional.empty());

        Optional<CustomerResponse> result = customerService.findById(id);

        assertTrue(result.isEmpty());
    }

    @Test
    void update_shouldUpdateCustomerWhenFound() {
        Long id = 1L;
        AddressUpdateRequest addressUpdateRequest = new AddressUpdateRequest("456 Oak Ave", "Othertown", "67890");
        CustomerUpdateRequest request = new CustomerUpdateRequest("Jane", "Doe", "jane.doe@example.com", addressUpdateRequest);
        Customer existingCustomer = new Customer();
        existingCustomer.setAddress(new Address());
        CustomerResponse customerResponse = new CustomerResponse(1L, "Jane", "Doe", "jane.doe@example.com", "456 Oak Ave, Othertown, 67890");

        when(customerRepository.findById(id)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.save(existingCustomer)).thenReturn(existingCustomer);
        when(modelMapper.map(existingCustomer, CustomerResponse.class)).thenReturn(customerResponse);

        CustomerResponse result = customerService.update(id, request);

        assertEquals(customerResponse, result);
        verify(modelMapper).map(request, existingCustomer);
        verify(modelMapper).map(request.address(), existingCustomer.getAddress());
        verify(customerRepository).save(existingCustomer);
    }

    @Test
    void update_shouldThrowExceptionWhenNotFound() {
        Long id = 1L;
        AddressUpdateRequest addressUpdateRequest = new AddressUpdateRequest("456 Oak Ave", "Othertown", "67890");
        CustomerUpdateRequest request = new CustomerUpdateRequest("Jane", "Doe", "jane.doe@example.com", addressUpdateRequest);

        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> customerService.update(id, request));
    }

    @Test
    void deleteById_shouldDeleteCustomer() {
        Long id = 1L;
        doNothing().when(customerRepository).deleteById(id);

        customerService.deleteById(id);

        verify(customerRepository).deleteById(id);
    }
}
