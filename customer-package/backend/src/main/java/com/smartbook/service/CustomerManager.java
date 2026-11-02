package com.smartbook.service;

import com.smartbook.dto.CustomerRequest;
import com.smartbook.dto.CustomerResponse;
import com.smartbook.entity.BusinessOwner;
import com.smartbook.entity.Customer;
import com.smartbook.repository.BusinessOwnerRepository;
import com.smartbook.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerManager {

    private final CustomerRepository customerRepository;
    private final BusinessOwnerRepository businessOwnerRepository;

    public CustomerManager(
            CustomerRepository customerRepository,
            BusinessOwnerRepository businessOwnerRepository) {

        this.customerRepository = customerRepository;
        this.businessOwnerRepository = businessOwnerRepository;
    }

    @Transactional
    public CustomerResponse createCustomer(
            String ownerEmail,
            CustomerRequest request) {

        BusinessOwner owner = businessOwnerRepository
                .findByEmail(ownerEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("Business owner not found"));

        if (customerRepository
                .findByBusinessOwnerAndCustomerEmail(
                        owner, request.customerEmail())
                .isPresent()) {

            throw new IllegalArgumentException(
                    "This customer already exists");
        }

        Customer customer = new Customer();

        customer.setBusinessOwner(owner);
        customer.setCustomerName(request.customerName());
        customer.setCustomerEmail(request.customerEmail());
        customer.setCustomerPhone(request.customerPhone());

        Customer savedCustomer = customerRepository.save(customer);

        return toResponse(savedCustomer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getMyCustomers(String ownerEmail) {

        BusinessOwner owner = businessOwnerRepository
                .findByEmail(ownerEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("Business owner not found"));

        return customerRepository
                .findByBusinessOwnerOrderByCustomerNameAsc(owner)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CustomerResponse toResponse(Customer customer) {

        return new CustomerResponse(
                customer.getId(),
                customer.getCustomerName(),
                customer.getCustomerEmail(),
                customer.getCustomerPhone(),
                customer.getCreatedAt()
        );
    }
}