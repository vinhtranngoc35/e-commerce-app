package com.example.customer.service;

import com.example.customer.dto.request.CustomerCreateRequest;
import com.example.customer.dto.request.CustomerUpdateRequest;
import com.example.customer.dto.response.CustomerResponse;
import com.example.customer.entity.Address;
import com.example.customer.entity.Customer;
import com.example.customer.repository.ICustomerRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService implements ICustomerService{
    private final ICustomerRepository customerRepository;
    private final ModelMapper modelMapper;
    @Override
    public CustomerResponse create(CustomerCreateRequest request) {
        Customer customer = modelMapper.map(request, Customer.class);
        Address address = modelMapper.map(request.address(), Address.class);

        customer.setAddress(address);

        Customer savedCustomer = customerRepository.save(customer);

        return modelMapper.map(savedCustomer, CustomerResponse.class);
    }

    @Override
    public Page<CustomerResponse> findAll(Pageable pageable) {
        return customerRepository.findAllCustomerResponse(pageable);
    }

    @Override
    public Optional<CustomerResponse> findById(Long id) {
        return customerRepository.findCustomerResponseById(id);
    }

    @Override
    public CustomerResponse update(Long id, CustomerUpdateRequest request) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id " + id));

        modelMapper.map(request, existingCustomer);
        modelMapper.map(request.address(), existingCustomer.getAddress());

        Customer updatedCustomer = customerRepository.save(existingCustomer);

        return modelMapper.map(updatedCustomer, CustomerResponse.class);
    }

    @Override
    public void deleteById(Long id) {
        customerRepository.deleteById(id);
    }
}
