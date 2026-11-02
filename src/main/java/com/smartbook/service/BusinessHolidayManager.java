package com.smartbook.service;

import com.smartbook.dto.BusinessHolidayRequest;
import com.smartbook.dto.BusinessHolidayResponse;
import com.smartbook.entity.BusinessHoliday;
import com.smartbook.entity.BusinessOwner;
import com.smartbook.repository.BusinessHolidayRepository;
import com.smartbook.repository.BusinessOwnerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BusinessHolidayManager {

    private final BusinessHolidayRepository holidayRepository;
    private final BusinessOwnerRepository ownerRepository;

    public BusinessHolidayManager(
            BusinessHolidayRepository holidayRepository,
            BusinessOwnerRepository ownerRepository
    ) {
        this.holidayRepository = holidayRepository;
        this.ownerRepository = ownerRepository;
    }

    public List<BusinessHolidayResponse> getHolidays(String ownerEmail) {

        BusinessOwner owner = ownerRepository.findByEmail(ownerEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("Business owner not found."));

        return holidayRepository
                .findByBusinessOwnerOrderByHolidayDateAsc(owner)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public BusinessHolidayResponse addHoliday(
            String ownerEmail,
            BusinessHolidayRequest request
    ) {
        BusinessOwner owner = ownerRepository.findByEmail(ownerEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("Business owner not found."));

        if (holidayRepository
                .findByBusinessOwnerAndHolidayDate(owner, request.holidayDate())
                .isPresent()) {
            throw new IllegalArgumentException(
                    "A holiday already exists for this date."
            );
        }

        BusinessHoliday holiday = new BusinessHoliday();
        holiday.setBusinessOwner(owner);
        holiday.setHolidayDate(request.holidayDate());
        holiday.setName(request.name());

        return toResponse(holidayRepository.save(holiday));
    }

    public void deleteHoliday(String ownerEmail, Long holidayId) {

        BusinessOwner owner = ownerRepository.findByEmail(ownerEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("Business owner not found."));

        BusinessHoliday holiday = holidayRepository
                .findByIdAndBusinessOwner(holidayId, owner)
                .orElseThrow(() ->
                        new IllegalArgumentException("Holiday not found."));

        holidayRepository.delete(holiday);
    }

    private BusinessHolidayResponse toResponse(BusinessHoliday holiday) {
        return new BusinessHolidayResponse(
                holiday.getId(),
                holiday.getHolidayDate(),
                holiday.getName()
        );
    }
}