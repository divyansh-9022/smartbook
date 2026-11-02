package com.smartbook.controller;

import com.smartbook.entity.BusinessService;
import com.smartbook.service.BusinessServiceManager;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final BusinessServiceManager serviceManager;

    public ServiceController(BusinessServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    public record ServiceRequest(
            @NotBlank String serviceName,
            String description,
            @NotNull @DecimalMin("0.01") BigDecimal price,
            @NotNull @Positive Integer durationMinutes
    ) {}

    public record ServiceResponse(
            Long id,
            String serviceName,
            String description,
            BigDecimal price,
            Integer durationMinutes,
            boolean active
    ) {}

    @PostMapping
    public ServiceResponse addService(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ServiceRequest request) {

        BusinessService service = serviceManager.addService(
                jwt.getSubject(),
                request.serviceName(),
                request.description(),
                request.price(),
                request.durationMinutes()
        );

        return toResponse(service);
    }

    @GetMapping
    public List<ServiceResponse> getMyServices(
            @AuthenticationPrincipal Jwt jwt) {

        return serviceManager.getMyServices(jwt.getSubject())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ServiceResponse toResponse(BusinessService service) {
        return new ServiceResponse(
                service.getId(),
                service.getServiceName(),
                service.getDescription(),
                service.getPrice(),
                service.getDurationMinutes(),
                service.isActive()
        );
    }
    @PutMapping("/{id}")
    public ServiceResponse updateService(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody ServiceRequest request) {

        BusinessService service = serviceManager.updateService(
                jwt.getSubject(),
                id,
                request.serviceName(),
                request.description(),
                request.price(),
                request.durationMinutes()
        );

        return toResponse(service);
    }

    @PatchMapping("/{id}/deactivate")
    public void deactivateService(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {

        serviceManager.deactivateService(jwt.getSubject(), id);
    }
}