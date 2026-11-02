package com.smartbook.repository;

import com.smartbook.entity.BusinessBreak;
import com.smartbook.entity.BusinessOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface BusinessBreakRepository
        extends JpaRepository<BusinessBreak, Long> {

    List<BusinessBreak> findByBusinessOwnerOrderByDayOfWeekAscStartTimeAsc(
            BusinessOwner businessOwner
    );

    List<BusinessBreak> findByBusinessOwnerAndDayOfWeek(
            BusinessOwner businessOwner,
            DayOfWeek dayOfWeek
    );

    Optional<BusinessBreak> findByIdAndBusinessOwner(
            Long id,
            BusinessOwner businessOwner
    );
}