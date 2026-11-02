
package com.smartbook.service;

import com.smartbook.dto.AvailableSlotResponse;
import com.smartbook.dto.BusinessHoursResponse;
import com.smartbook.entity.Appointment;
import com.smartbook.entity.BusinessOwner;
import com.smartbook.entity.BusinessService;
import com.smartbook.repository.AppointmentRepository;
import com.smartbook.repository.BusinessOwnerRepository;
import com.smartbook.repository.BusinessServiceRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.smartbook.repository.BusinessHolidayRepository;
import com.smartbook.entity.BusinessBreak;
import com.smartbook.repository.BusinessBreakRepository;

@Service
public class AvailabilityManager {

    private static final int SLOT_INTERVAL_MINUTES = 15;

    private final AppointmentRepository appointmentRepository;
    private final BusinessOwnerRepository ownerRepository;
    private final BusinessServiceRepository serviceRepository;
    private final BusinessHoursManager businessHoursManager;
    private final BusinessHolidayRepository holidayRepository;
    private final BusinessBreakRepository businessBreakRepository;

    public AvailabilityManager(
            AppointmentRepository appointmentRepository,
            BusinessOwnerRepository ownerRepository,
            BusinessServiceRepository serviceRepository,
            BusinessHoursManager businessHoursManager,
            BusinessHolidayRepository holidayRepository,
            BusinessBreakRepository businessBreakRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.ownerRepository = ownerRepository;
        this.serviceRepository = serviceRepository;
        this.businessHoursManager = businessHoursManager;
        this.holidayRepository = holidayRepository;
        this.businessBreakRepository = businessBreakRepository;
    }

    @Transactional
    public List<AvailableSlotResponse> getAvailableSlots(
            Long businessOwnerId,
            Long serviceId,
            LocalDate date) {

        BusinessOwner owner = ownerRepository.findById(businessOwnerId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Business not found"));

        BusinessService service = serviceRepository
                .findByIdAndBusinessOwner(serviceId, owner)
                .orElseThrow(() ->
                        new IllegalArgumentException("Service not found"));

        if (!service.isActive()) {
            throw new IllegalArgumentException(
                    "This service is currently unavailable");
        }
        boolean isHoliday = holidayRepository
                .findByBusinessOwnerAndHolidayDate(owner, date)
                .isPresent();

        if (isHoliday) {
            return List.of();
        }

        BusinessHoursResponse hours =
                businessHoursManager.getBusinessHoursForDate(
                        owner.getEmail(), date);

        if (hours.closed()) {
            return List.of();
        }

        LocalTime openingTime = hours.openTime();
        LocalTime closingTime = hours.closeTime();
        List<BusinessBreak> businessBreaks =
                businessBreakRepository.findByBusinessOwnerAndDayOfWeek(
                        owner,
                        date.getDayOfWeek()
                );

        int duration = service.getDurationMinutes();

        LocalDateTime searchStart = date.atStartOfDay();

        LocalDateTime searchEnd = date.plusDays(1)
                .atStartOfDay()
                .minusNanos(1);

        List<Appointment> existingAppointments =
                appointmentRepository
                        .findByBusinessOwnerAndStatusAndAppointmentDateTimeBetween(
                                owner,
                                "BOOKED",
                                searchStart,
                                searchEnd
                        );

        List<AvailableSlotResponse> availableSlots =
                new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime candidate = date.atTime(openingTime);

        LocalDateTime closingDateTime = date.atTime(closingTime);

        while (!candidate.plusMinutes(duration)
                .isAfter(closingDateTime)) {

            LocalDateTime candidateEnd =
                    candidate.plusMinutes(duration);

            boolean overlaps = false;

            for (Appointment existing : existingAppointments) {

                LocalDateTime existingStart =
                        existing.getAppointmentDateTime();

                LocalDateTime existingEnd = existingStart.plusMinutes(
                        existing.getBusinessService().getDurationMinutes());

                if (candidate.isBefore(existingEnd)
                        && candidateEnd.isAfter(existingStart)) {

                    overlaps = true;
                    break;
                }
            }

            for (BusinessBreak businessBreak : businessBreaks) {

                boolean overlapsBreak =
                        candidate.toLocalTime().isBefore(businessBreak.getEndTime())
                                && candidateEnd.toLocalTime().isAfter(businessBreak.getStartTime());

                if (overlapsBreak) {
                    overlaps = true;
                    break;
                }
            }

            if (!overlaps && candidate.isAfter(now)) {
                availableSlots.add(
                        new AvailableSlotResponse(candidate)
                );
            }

            candidate = candidate.plusMinutes(SLOT_INTERVAL_MINUTES);
        }

        return availableSlots;
        }
    }