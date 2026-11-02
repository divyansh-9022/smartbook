package com.smartbook.repository;

import com.smartbook.entity.BusinessService;
import com.smartbook.entity.BusinessOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessServiceRepository
        extends JpaRepository<BusinessService, Long> {

    List<BusinessService> findByBusinessOwner(BusinessOwner businessOwner);

    Optional<BusinessService> findByIdAndBusinessOwner(
            Long id,
            BusinessOwner businessOwner
    );
}