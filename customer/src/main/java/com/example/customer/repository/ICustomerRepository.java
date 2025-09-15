package com.example.customer.repository;

import com.example.customer.dto.response.CustomerResponse;
import com.example.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ICustomerRepository extends JpaRepository<Customer, Long> {
    @Query("SELECT NEW com.example.customer.dto.response.CustomerResponse(c.id, c.firstname, c.lastname, c.email, CONCAT(a.houseNumber, ', ', a.street, ', ', a.zipCode)) " +
            "FROM Customer c JOIN c.address a WHERE c.id = :id")
    Optional<CustomerResponse> findCustomerResponseById(@Param("id") Long id);

    @Query("SELECT NEW com.example.customer.dto.response.CustomerResponse(c.id, c.firstname, c.lastname, c.email, CONCAT(a.houseNumber, ', ', a.street, ', ', a.zipCode)) " +
            "FROM Customer c JOIN c.address a")
    Page<CustomerResponse> findAllCustomerResponse(Pageable pageable);
}
