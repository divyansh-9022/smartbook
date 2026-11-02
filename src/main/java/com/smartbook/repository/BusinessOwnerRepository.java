package com.smartbook.repository;

import com.smartbook.entity.BusinessOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessOwnerRepository
        extends JpaRepository<BusinessOwner, Long> {

    Optional<BusinessOwner> findByEmail(String email);

    boolean existsByEmail(String email);
    Optional<BusinessOwner> findByBookingSlug(String bookingSlug);

    boolean existsByBookingSlug(String bookingSlug);
}