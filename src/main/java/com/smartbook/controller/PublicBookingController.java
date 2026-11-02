
package com.smartbook.controller;

import com.smartbook.dto.AppointmentRequest;
import com.smartbook.dto.AppointmentResponse;
import com.smartbook.entity.BusinessOwner;
import com.smartbook.entity.BusinessService;
import com.smartbook.repository.BusinessOwnerRepository;
import com.smartbook.repository.BusinessServiceRepository;
import com.smartbook.service.AppointmentManager;
import com.smartbook.service.AvailabilityManager;
import java.time.LocalDate;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/public/businesses")
public class PublicBookingController {

    private final BusinessOwnerRepository ownerRepository;
    private final BusinessServiceRepository serviceRepository;
    private final AppointmentManager appointmentManager;
    private final AvailabilityManager availabilityManager;


    public PublicBookingController(
            BusinessOwnerRepository ownerRepository,
            BusinessServiceRepository serviceRepository,
            AppointmentManager appointmentManager,
            AvailabilityManager availabilityManager) {

        this.ownerRepository = ownerRepository;
        this.serviceRepository = serviceRepository;
        this.appointmentManager = appointmentManager;
        this.availabilityManager = availabilityManager;
    }
    public record PublicBusinessResponse(
            Long id,
            String businessName,
            String logoUrl
    ) {}

    @GetMapping("/{businessOwnerId}")
    public PublicBusinessResponse getPublicBusiness(
            @PathVariable String businessOwnerId) {

        BusinessOwner owner = findBusiness(businessOwnerId);

        return new PublicBusinessResponse(
                owner.getId(),
                owner.getBusinessName(),
                owner.getLogoUrl()
        );
    }



    public record PublicServiceResponse(
            Long id,
            String serviceName,
            String description,
            BigDecimal price,
            Integer durationMinutes
    ) {}

    @GetMapping("/{businessOwnerId}/services")
    public List<PublicServiceResponse> getPublicServices(
            @PathVariable String businessOwnerId) {

        BusinessOwner owner = findBusiness(businessOwnerId);

        return serviceRepository.findByBusinessOwner(owner)
                .stream()
                .filter(BusinessService::isActive)
                .map(service -> new PublicServiceResponse(
                        service.getId(),
                        service.getServiceName(),
                        service.getDescription(),
                        service.getPrice(),
                        service.getDurationMinutes()
                ))
                .toList();
    }

    @PostMapping("/{businessOwnerId}/appointments")
    public AppointmentResponse bookPublicAppointment(
            @PathVariable String businessOwnerId,
            @Valid @RequestBody AppointmentRequest request) {

        BusinessOwner owner = findBusiness(businessOwnerId);

        return appointmentManager.bookPublicAppointment(
                owner.getId(),
                request
        );
    }





    @GetMapping("/{businessOwnerId}/available-slots")
    public List<com.smartbook.dto.AvailableSlotResponse> getAvailableSlots(
            @PathVariable String businessOwnerId,
            @RequestParam Long serviceId,
            @RequestParam LocalDate date) {

        BusinessOwner owner = findBusiness(businessOwnerId);

        return availabilityManager.getAvailableSlots(
                owner.getId(),
                serviceId,
                date
        );
    }
    private BusinessOwner findBusiness(String identifier) {

        if (identifier.matches("\\d+")) {
            return ownerRepository.findById(Long.valueOf(identifier))
                    .orElseThrow(() ->
                            new IllegalArgumentException("Business not found"));
        }

        return ownerRepository.findByBookingSlug(identifier)
                .orElseThrow(() ->
                        new IllegalArgumentException("Business not found"));
    }

}
