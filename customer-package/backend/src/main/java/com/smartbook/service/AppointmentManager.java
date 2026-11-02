
package com.smartbook.service;

import com.smartbook.dto.AppointmentRequest;
import com.smartbook.dto.AppointmentResponse;
import com.smartbook.entity.Appointment;
import com.smartbook.entity.BusinessOwner;
import com.smartbook.entity.BusinessService;
import com.smartbook.repository.AppointmentRepository;
import com.smartbook.repository.BusinessOwnerRepository;
import com.smartbook.repository.BusinessServiceRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import com.smartbook.repository.BusinessHolidayRepository;
import java.time.LocalDate;

@Service
public class AppointmentManager {

    private final AppointmentRepository appointmentRepository;
    private final BusinessOwnerRepository ownerRepository;
    private final BusinessServiceRepository serviceRepository;
    private final EmailService emailService;
    private final BusinessHoursManager businessHoursManager;
    private final BusinessHolidayRepository holidayRepository;

    public AppointmentManager(
            AppointmentRepository appointmentRepository,
            BusinessOwnerRepository ownerRepository,
            BusinessServiceRepository serviceRepository,
            EmailService emailService,
            BusinessHoursManager businessHoursManager,
    BusinessHolidayRepository holidayRepository) {

        this.appointmentRepository = appointmentRepository;
        this.ownerRepository = ownerRepository;
        this.serviceRepository = serviceRepository;
        this.emailService = emailService;
        this.businessHoursManager = businessHoursManager;
        this.holidayRepository = holidayRepository;
    }

    @Transactional
    public AppointmentResponse bookAppointment(
            String ownerEmail,
            AppointmentRequest request) {

        BusinessOwner owner = ownerRepository.findByEmail(ownerEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("Business owner not found"));

        BusinessService businessService =
                serviceRepository.findByIdAndBusinessOwner(
                                request.serviceId(), owner)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Service not found"));

        if (!businessService.isActive()) {
            throw new IllegalArgumentException(
                    "This service is currently unavailable");
        }

        LocalDateTime newStart = request.appointmentDateTime();
        validateNotHoliday(owner, newStart.toLocalDate());

        LocalDateTime newEnd = newStart.plusMinutes(
                businessService.getDurationMinutes());


// Check business operating hours
        businessHoursManager.validateAppointmentTime(
                ownerEmail,
                newStart,
                newEnd
        );

// Check overlapping appointments
        checkAvailability(owner, newStart, newEnd, null);

        Appointment appointment = new Appointment();

        appointment.setBusinessOwner(owner);
        appointment.setBusinessService(businessService);
        appointment.setCustomerName(request.customerName());
        appointment.setCustomerEmail(request.customerEmail());
        appointment.setCustomerPhone(request.customerPhone());
        appointment.setAppointmentDateTime(request.appointmentDateTime());
        scheduleReminder(appointment, newStart);
        appointment.setStatus("BOOKED");

        Appointment saved = appointmentRepository.save(appointment);

        try {
            System.out.println(
                    "SMARTBOOK: Attempting to send confirmation email."
            );

            emailService.sendAppointmentConfirmation(
                    saved.getCustomerEmail(),
                    saved.getCustomerName(),
                    saved.getBusinessService().getServiceName(),
                    saved.getAppointmentDateTime()
            );

            System.out.println(
                    "SMARTBOOK: Email sent successfully."
            );

        } catch (Exception exception) {
            System.err.println("SMARTBOOK: Email sending failed.");
            exception.printStackTrace();
        }

        return toResponse(saved);
    }
    @Transactional
    public AppointmentResponse bookPublicAppointment(
            Long businessOwnerId,
            AppointmentRequest request) {

        BusinessOwner owner = ownerRepository.findById(businessOwnerId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Business not found"));

        return bookAppointment(owner.getEmail(), request);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointments(String ownerEmail) {

        BusinessOwner owner = ownerRepository.findByEmail(ownerEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("Business owner not found"));

        return appointmentRepository
                .findByBusinessOwnerOrderByAppointmentDateTimeAsc(owner)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getCustomerAppointmentHistory(
            String ownerEmail,
            String customerEmail) {

        BusinessOwner owner = ownerRepository.findByEmail(ownerEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("Business owner not found"));

        return appointmentRepository
                .findByBusinessOwnerAndCustomerEmailOrderByAppointmentDateTimeDesc(
                        owner, customerEmail)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void cancelAppointment(
            String ownerEmail,
            Long appointmentId) {

        BusinessOwner owner = ownerRepository.findByEmail(ownerEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("Business owner not found"));

        Appointment appointment = appointmentRepository
                .findByIdAndBusinessOwner(appointmentId, owner)
                .orElseThrow(() ->
                        new IllegalArgumentException("Appointment not found"));

        if ("CANCELLED".equals(appointment.getStatus())) {
            throw new IllegalArgumentException(
                    "Appointment is already cancelled");
        }

        appointment.setStatus("CANCELLED");
        appointmentRepository.save(appointment);
    }

    @Transactional
    public AppointmentResponse rescheduleAppointment(
            String ownerEmail,
            Long appointmentId,
            LocalDateTime newDateTime) {

        BusinessOwner owner = ownerRepository.findByEmail(ownerEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("Business owner not found"));

        Appointment appointment = appointmentRepository
                .findByIdAndBusinessOwner(appointmentId, owner)
                .orElseThrow(() ->
                        new IllegalArgumentException("Appointment not found"));

        if ("CANCELLED".equals(appointment.getStatus())) {
            throw new IllegalArgumentException(
                    "Cancelled appointments cannot be rescheduled");
        }

        LocalDateTime newEnd = newDateTime.plusMinutes(
                appointment.getBusinessService().getDurationMinutes());
        validateNotHoliday(owner, newDateTime.toLocalDate());

        checkAvailability(
                owner,
                newDateTime,
                newEnd,
                appointment.getId()
        );

        appointment.setAppointmentDateTime(newDateTime);
        scheduleReminder(appointment, newDateTime);

        Appointment savedAppointment =
                appointmentRepository.save(appointment);

        return toResponse(savedAppointment);
    }

    private AppointmentResponse toResponse(Appointment appointment) {

        BusinessService service = appointment.getBusinessService();

        return new AppointmentResponse(
                appointment.getId(),
                service.getServiceName(),
                service.getPrice(),
                service.getDurationMinutes(),
                appointment.getCustomerName(),
                appointment.getCustomerEmail(),
                appointment.getCustomerPhone(),
                appointment.getAppointmentDateTime(),
                appointment.getStatus(),
                appointment.isReminderSent()
        );
    }
    private void validateNotHoliday(BusinessOwner owner, LocalDate date) {

        boolean isHoliday = holidayRepository
                .findByBusinessOwnerAndHolidayDate(owner, date)
                .isPresent();

        if (isHoliday) {
            throw new IllegalArgumentException(
                    "The business is closed for a holiday on this date."
            );
        }
    }

    private void checkAvailability(
            BusinessOwner owner,
            LocalDateTime newStart,
            LocalDateTime newEnd,
            Long excludedAppointmentId) {

        List<Appointment> bookedAppointments =
                appointmentRepository.findByBusinessOwnerAndStatus(
                        owner, "BOOKED");

        for (Appointment existing : bookedAppointments) {

            if (excludedAppointmentId != null
                    && existing.getId().equals(excludedAppointmentId)) {
                continue;
            }

            LocalDateTime existingStart =
                    existing.getAppointmentDateTime();

            LocalDateTime existingEnd =
                    existingStart.plusMinutes(
                            existing.getBusinessService().getDurationMinutes());

            boolean overlaps =
                    newStart.isBefore(existingEnd)
                            && newEnd.isAfter(existingStart);

            if (overlaps) {
                throw new IllegalArgumentException(
                        "This time slot is already booked");
            }
        }
    }
    private void scheduleReminder(
            Appointment appointment,
            LocalDateTime appointmentDateTime
    ) {
        LocalDateTime reminderTime = appointmentDateTime.minusHours(24);

        appointment.setReminderSent(false);

        if (reminderTime.isAfter(LocalDateTime.now())) {
            appointment.setReminderDueAt(reminderTime);
        } else {
            appointment.setReminderDueAt(null);
        }
    }
}
