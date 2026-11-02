
package com.smartbook.service;

import com.smartbook.entity.Appointment;
import com.smartbook.repository.AppointmentRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AppointmentReminderScheduler {

    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;

    public AppointmentReminderScheduler(
            AppointmentRepository appointmentRepository,
            EmailService emailService) {

        this.appointmentRepository = appointmentRepository;
        this.emailService = emailService;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void sendAppointmentReminders() {

        LocalDateTime now = LocalDateTime.now();

        List<Appointment> appointments =
                appointmentRepository
                        .findByStatusAndReminderSentFalseAndReminderDueAtLessThanEqual(
                                "BOOKED", now);

        for (Appointment appointment : appointments) {
            if (!appointment.getBusinessService()
                    .getBusinessOwner()
                    .isEmailRemindersEnabled()) {
                continue;
            }

            try {
                emailService.sendAppointmentReminder(
                        appointment.getCustomerEmail(),
                        appointment.getCustomerName(),
                        appointment.getBusinessService().getServiceName(),
                        appointment.getAppointmentDateTime()
                );

                appointment.setReminderSent(true);
                appointmentRepository.save(appointment);

                System.out.println(
                        "SMARTBOOK: Appointment reminder sent for ID "
                                + appointment.getId()
                );

            } catch (Exception exception) {

                System.err.println(
                        "SMARTBOOK: Reminder email failed for appointment ID "
                                + appointment.getId()
                );

                exception.printStackTrace();
            }
        }
    }
}
