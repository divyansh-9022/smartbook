package com.smartbook.repository;

import com.smartbook.entity.BusinessOwner;
import com.smartbook.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

    List<Customer> findByBusinessOwnerOrderByCustomerNameAsc(
            BusinessOwner businessOwner
    );

    Optional<Customer> findByBusinessOwnerAndCustomerEmail(
            BusinessOwner businessOwner,
            String customerEmail
    );

    Optional<Customer> findByIdAndBusinessOwner(
            Long id,
            BusinessOwner businessOwner
    );
}