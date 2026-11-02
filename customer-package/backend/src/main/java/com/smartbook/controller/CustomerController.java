package com.smartbook.controller;

import com.smartbook.dto.CustomerRequest;
import com.smartbook.dto.CustomerResponse;
import com.smartbook.service.CustomerManager;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerManager customerManager;

    public CustomerController(CustomerManager customerManager) {
        this.customerManager = customerManager;
    }

    @PostMapping
    public CustomerResponse createCustomer(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CustomerRequest request) {

        String ownerEmail = jwt.getSubject();

        return customerManager.createCustomer(ownerEmail, request);
    }

    @GetMapping
    public List<CustomerResponse> getMyCustomers(
            @AuthenticationPrincipal Jwt jwt) {

        String ownerEmail = jwt.getSubject();

        return customerManager.getMyCustomers(ownerEmail);
    }
}