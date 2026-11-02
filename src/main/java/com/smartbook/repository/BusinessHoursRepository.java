
package com.smartbook.repository;

import com.smartbook.entity.BusinessHours;
import com.smartbook.entity.BusinessOwner;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface BusinessHoursRepository
        extends JpaRepository<BusinessHours, Long> {

    List<BusinessHours> findByBusinessOwnerOrderByDayOfWeekAsc(
            BusinessOwner businessOwner
    );

    Optional<BusinessHours> findByBusinessOwnerAndDayOfWeek(
            BusinessOwner businessOwner,
            DayOfWeek dayOfWeek
    );
}
