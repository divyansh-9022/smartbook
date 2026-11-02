package com.smartbook.service;

import com.smartbook.entity.BusinessOwner;
import com.smartbook.entity.BusinessService;
import com.smartbook.repository.BusinessOwnerRepository;
import com.smartbook.repository.BusinessServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BusinessServiceManager {

    private final BusinessServiceRepository serviceRepository;
    private final BusinessOwnerRepository ownerRepository;

    public BusinessServiceManager(
            BusinessServiceRepository serviceRepository,
            BusinessOwnerRepository ownerRepository) {

        this.serviceRepository = serviceRepository;
        this.ownerRepository = ownerRepository;
    }

    @Transactional
    public BusinessService addService(
            String email,
            String serviceName,
            String description,
            BigDecimal price,
            Integer durationMinutes) {

        BusinessOwner owner = ownerRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("Business owner not found"));

        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }

        if (durationMinutes == null || durationMinutes <= 0) {
            throw new IllegalArgumentException("Duration must be greater than zero");
        }

        BusinessService service = new BusinessService();

        service.setServiceName(serviceName);
        service.setDescription(description);
        service.setPrice(price);
        service.setDurationMinutes(durationMinutes);
        service.setBusinessOwner(owner);

        return serviceRepository.save(service);
    }

    @Transactional(readOnly = true)
    public List<BusinessService> getMyServices(String email) {

        BusinessOwner owner = ownerRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("Business owner not found"));

        return serviceRepository.findByBusinessOwner(owner);
    }

    @Transactional
    public BusinessService updateService(
            String email,
            Long serviceId,
            String serviceName,
            String description,
            BigDecimal price,
            Integer durationMinutes) {

        BusinessOwner owner = ownerRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("Business owner not found"));

        BusinessService service = serviceRepository
                .findByIdAndBusinessOwner(serviceId, owner)
                .orElseThrow(() ->
                        new IllegalArgumentException("Service not found"));

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }

        if (durationMinutes == null || durationMinutes <= 0) {
            throw new IllegalArgumentException("Duration must be greater than zero");
        }

        service.setServiceName(serviceName);
        service.setDescription(description);
        service.setPrice(price);
        service.setDurationMinutes(durationMinutes);

        return serviceRepository.save(service);
    }

    @Transactional
    public void deactivateService(String email, Long serviceId) {

        BusinessOwner owner = ownerRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("Business owner not found"));

        BusinessService service = serviceRepository
                .findByIdAndBusinessOwner(serviceId, owner)
                .orElseThrow(() ->
                        new IllegalArgumentException("Service not found"));

        service.setActive(false);

        serviceRepository.save(service);
    }
}