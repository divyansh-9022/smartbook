package com.smartbook.repository;

import com.smartbook.entity.LicenseActivation;
import com.smartbook.entity.BusinessOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LicenseActivationRepository
        extends JpaRepository<LicenseActivation, Long> {

    Optional<LicenseActivation> findByBusinessOwner(
            BusinessOwner businessOwner
    );

    boolean existsByLicenseKeyHash(String licenseKeyHash);
}