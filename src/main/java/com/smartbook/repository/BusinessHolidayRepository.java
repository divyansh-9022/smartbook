package com.smartbook.repository;

import com.smartbook.entity.BusinessHoliday;
import com.smartbook.entity.BusinessOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BusinessHolidayRepository
        extends JpaRepository<BusinessHoliday, Long> {

    List<BusinessHoliday> findByBusinessOwnerOrderByHolidayDateAsc(
            BusinessOwner businessOwner
    );

    Optional<BusinessHoliday> findByBusinessOwnerAndHolidayDate(
            BusinessOwner businessOwner,
            LocalDate holidayDate
    );

    Optional<BusinessHoliday> findByIdAndBusinessOwner(
            Long id,
            BusinessOwner businessOwner
    );
}