package com.smartbook.repository;

import java.time.LocalDateTime;
import com.smartbook.entity.Appointment;
import com.smartbook.entity.BusinessOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    List<Appointment> findByBusinessOwnerOrderByAppointmentDateTimeAsc(
            BusinessOwner businessOwner
    );

    Optional<Appointment> findByIdAndBusinessOwner(
            Long id,
            BusinessOwner businessOwner
    );

    List<Appointment> findByBusinessOwnerAndStatus(
            BusinessOwner businessOwner,
            String status
    );
    List<Appointment> findByBusinessOwnerAndCustomerEmailOrderByAppointmentDateTimeDesc(
            BusinessOwner businessOwner,
            String customerEmail
    );
    List<Appointment> findByStatusAndReminderSentFalseAndAppointmentDateTimeBetween(
            String status,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Appointment> findByBusinessOwnerAndStatusAndAppointmentDateTimeBetween(
            BusinessOwner businessOwner,
            String status,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Appointment> findByStatusAndReminderSentFalseAndReminderDueAtLessThanEqual(
            String status,
            LocalDateTime reminderDueAt
    );

}