package com.smartbook.service;

import com.smartbook.dto.BusinessBreakRequest;
import com.smartbook.dto.BusinessBreakResponse;
import com.smartbook.dto.BusinessHoursResponse;
import com.smartbook.entity.BusinessBreak;
import com.smartbook.entity.BusinessOwner;
import com.smartbook.repository.BusinessBreakRepository;
import com.smartbook.repository.BusinessOwnerRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BusinessBreakManager {

    private final BusinessBreakRepository breakRepository;
    private final BusinessOwnerRepository ownerRepository;
    private final BusinessHoursManager businessHoursManager;

    public BusinessBreakManager(
            BusinessBreakRepository breakRepository,
            BusinessOwnerRepository ownerRepository,
            BusinessHoursManager businessHoursManager
    ) {
        this.breakRepository = breakRepository;
        this.ownerRepository = ownerRepository;
        this.businessHoursManager = businessHoursManager;
    }

    @Transactional(readOnly = true)
    public List<BusinessBreakResponse> getMyBreaks(String ownerEmail) {

        BusinessOwner owner = findOwner(ownerEmail);

        return breakRepository
                .findByBusinessOwnerOrderByDayOfWeekAscStartTimeAsc(owner)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BusinessBreakResponse addBreak(
            String ownerEmail,
            BusinessBreakRequest request
    ) {
        BusinessOwner owner = findOwner(ownerEmail);

        if (!request.endTime().isAfter(request.startTime())) {
            throw new IllegalArgumentException(
                    "Break end time must be after start time."
            );
        }

        BusinessHoursResponse hours =
                businessHoursManager.getBusinessHoursForDate(
                        ownerEmail,
                        java.time.LocalDate.now()
                                .with(request.dayOfWeek())
                );

        if (hours.closed()) {
            throw new IllegalArgumentException(
                    "Cannot schedule a break on a closed business day."
            );
        }

        if (request.startTime().isBefore(hours.openTime())
                || request.endTime().isAfter(hours.closeTime())) {
            throw new IllegalArgumentException(
                    "Break must be within business operating hours."
            );
        }

        List<BusinessBreak> existingBreaks =
                breakRepository.findByBusinessOwnerAndDayOfWeek(
                        owner,
                        request.dayOfWeek()
                );

        for (BusinessBreak existing : existingBreaks) {
            boolean overlaps =
                    request.startTime().isBefore(existing.getEndTime())
                            && request.endTime().isAfter(existing.getStartTime());

            if (overlaps) {
                throw new IllegalArgumentException(
                        "This break overlaps with an existing break."
                );
            }
        }

        BusinessBreak businessBreak = new BusinessBreak();
        businessBreak.setBusinessOwner(owner);
        businessBreak.setDayOfWeek(request.dayOfWeek());
        businessBreak.setStartTime(request.startTime());
        businessBreak.setEndTime(request.endTime());
        businessBreak.setBreakName(request.breakName().trim());

        return toResponse(breakRepository.save(businessBreak));
    }

    @Transactional
    public void deleteBreak(String ownerEmail, Long breakId) {

        BusinessOwner owner = findOwner(ownerEmail);

        BusinessBreak businessBreak = breakRepository
                .findByIdAndBusinessOwner(breakId, owner)
                .orElseThrow(() ->
                        new IllegalArgumentException("Break not found."));

        breakRepository.delete(businessBreak);
    }

    private BusinessOwner findOwner(String ownerEmail) {
        return ownerRepository.findByEmail(ownerEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("Business owner not found."));
    }

    private BusinessBreakResponse toResponse(BusinessBreak businessBreak) {
        return new BusinessBreakResponse(
                businessBreak.getId(),
                businessBreak.getDayOfWeek(),
                businessBreak.getStartTime(),
                businessBreak.getEndTime(),
                businessBreak.getBreakName()
        );
    }
}