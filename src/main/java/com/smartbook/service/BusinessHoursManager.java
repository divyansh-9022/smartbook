
package com.smartbook.service;

import com.smartbook.dto.BusinessHoursRequest;
import com.smartbook.dto.BusinessHoursResponse;
import com.smartbook.entity.BusinessHours;
import com.smartbook.entity.BusinessOwner;
import com.smartbook.repository.BusinessHoursRepository;
import com.smartbook.repository.BusinessOwnerRepository;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.time.LocalDateTime;
import com.smartbook.repository.BusinessBreakRepository;
import com.smartbook.entity.BusinessBreak;

@Service
public class BusinessHoursManager {

    private final BusinessHoursRepository businessHoursRepository;
    private final BusinessOwnerRepository ownerRepository;
    private final BusinessBreakRepository businessBreakRepository;


    public BusinessHoursManager(
            BusinessHoursRepository businessHoursRepository,
            BusinessOwnerRepository ownerRepository,
            BusinessBreakRepository businessBreakRepository) {

        this.businessHoursRepository = businessHoursRepository;
        this.ownerRepository = ownerRepository;
        this.businessBreakRepository = businessBreakRepository;
    }

    @Transactional
    public List<BusinessHoursResponse> getMyBusinessHours(
            String ownerEmail) {

        BusinessOwner owner = findOwner(ownerEmail);

        createDefaultHoursIfMissing(owner);

        return businessHoursRepository
                .findByBusinessOwnerOrderByDayOfWeekAsc(owner)
                .stream()
                .sorted(Comparator.comparing(BusinessHours::getDayOfWeek))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BusinessHoursResponse updateBusinessHours(
            String ownerEmail,
            BusinessHoursRequest request) {

        BusinessOwner owner = findOwner(ownerEmail);

        if (!request.closed()) {
            if (request.openTime() == null ||
                    request.closeTime() == null) {

                throw new IllegalArgumentException(
                        "Opening and closing times are required"
                );
            }

            if (!request.closeTime().isAfter(request.openTime())) {
                throw new IllegalArgumentException(
                        "Closing time must be after opening time"
                );
            }
        }

        createDefaultHoursIfMissing(owner);

        BusinessHours hours = businessHoursRepository
                .findByBusinessOwnerAndDayOfWeek(
                        owner, request.dayOfWeek())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Business hours not found"));

        hours.setClosed(request.closed());

        if (request.closed()) {
            hours.setOpenTime(null);
            hours.setCloseTime(null);
        } else {
            hours.setOpenTime(request.openTime());
            hours.setCloseTime(request.closeTime());
        }

        return toResponse(
                businessHoursRepository.save(hours)
        );
    }

    @Transactional
    public BusinessHoursResponse getBusinessHoursForDate(
            String ownerEmail,
            LocalDate date) {

        BusinessOwner owner = findOwner(ownerEmail);

        createDefaultHoursIfMissing(owner);

        BusinessHours hours = businessHoursRepository
                .findByBusinessOwnerAndDayOfWeek(
                        owner,
                        date.getDayOfWeek()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Business hours not configured"));

        return toResponse(hours);
    }


    private BusinessOwner findOwner(String ownerEmail) {
        return ownerRepository.findByEmail(ownerEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Business owner not found"));
    }

    private void createDefaultHoursIfMissing(BusinessOwner owner) {

        List<BusinessHours> existingHours =
                businessHoursRepository
                        .findByBusinessOwnerOrderByDayOfWeekAsc(owner);

        if (!existingHours.isEmpty()) {
            return;
        }

        List<BusinessHours> defaultHours = new ArrayList<>();

        for (DayOfWeek day : DayOfWeek.values()) {

            BusinessHours hours = new BusinessHours();

            hours.setBusinessOwner(owner);
            hours.setDayOfWeek(day);

            if (day == DayOfWeek.SUNDAY) {
                hours.setClosed(true);
                hours.setOpenTime(null);
                hours.setCloseTime(null);
            } else {
                hours.setClosed(false);
                hours.setOpenTime(LocalTime.of(9, 0));
                hours.setCloseTime(LocalTime.of(18, 0));
            }

            defaultHours.add(hours);
        }

        businessHoursRepository.saveAll(defaultHours);
    }

    private BusinessHoursResponse toResponse(BusinessHours hours) {

        return new BusinessHoursResponse(
                hours.getId(),
                hours.getDayOfWeek(),
                hours.getOpenTime(),
                hours.getCloseTime(),
                hours.isClosed()
        );
    }

    @Transactional
    public void validateAppointmentTime(
            String ownerEmail,
            LocalDateTime appointmentStart,
            LocalDateTime appointmentEnd
    ) {
        BusinessOwner owner = findOwner(ownerEmail);

        createDefaultHoursIfMissing(owner);

        BusinessHours hours = businessHoursRepository
                .findByBusinessOwnerAndDayOfWeek(
                        owner,
                        appointmentStart.getDayOfWeek()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Business hours not configured."
                        )
                );

        if (hours.isClosed()) {
            throw new IllegalArgumentException(
                    "The business is closed on this day."
            );
        }

        if (!appointmentStart.toLocalDate().equals(appointmentEnd.toLocalDate())) {
            throw new IllegalArgumentException(
                    "Appointments cannot extend into another day."
            );
        }

        if (appointmentStart.toLocalTime().isBefore(hours.getOpenTime())
                || appointmentStart.toLocalTime().isAfter(hours.getCloseTime())
                || appointmentEnd.toLocalTime().isAfter(hours.getCloseTime())) {
            throw new IllegalArgumentException(
                    "Appointment is outside business operating hours."
            );
        }

        List<BusinessBreak> breaks =
                businessBreakRepository.findByBusinessOwnerAndDayOfWeek(
                        owner,
                        appointmentStart.getDayOfWeek()
                );

        for (BusinessBreak businessBreak : breaks) {

            boolean overlaps =
                    appointmentStart.toLocalTime().isBefore(businessBreak.getEndTime())
                            && appointmentEnd.toLocalTime().isAfter(businessBreak.getStartTime());

            if (overlaps) {
                throw new IllegalArgumentException(
                        "This appointment overlaps with the business break: "
                                + businessBreak.getBreakName()
                );
            }
        }
    }

}
