package com.smartbook.controller;

import com.smartbook.dto.AppointmentRescheduleRequest;
import com.smartbook.dto.AppointmentRequest;
import com.smartbook.dto.AppointmentResponse;
import com.smartbook.service.AppointmentManager;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentManager appointmentManager;

    public AppointmentController(AppointmentManager appointmentManager) {
        this.appointmentManager = appointmentManager;
    }

    @PostMapping
    public AppointmentResponse bookAppointment(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AppointmentRequest request) {

        return appointmentManager.bookAppointment(
                jwt.getSubject(), request);
    }

    @GetMapping
    public List<AppointmentResponse> getMyAppointments(
            @AuthenticationPrincipal Jwt jwt) {

        return appointmentManager.getMyAppointments(
                jwt.getSubject());
    }
    @PatchMapping("/{id}/cancel")
    public void cancelAppointment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {

        appointmentManager.cancelAppointment(
                jwt.getSubject(), id);
    }
    @PatchMapping("/{id}/reschedule")
    public AppointmentResponse rescheduleAppointment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRescheduleRequest request) {

        return appointmentManager.rescheduleAppointment(
                jwt.getSubject(),
                id,
                request.appointmentDateTime()
        );
    }
    @GetMapping("/customer/{customerEmail}")
    public List<AppointmentResponse> getCustomerAppointmentHistory(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String customerEmail) {

        String ownerEmail = jwt.getSubject();

        return appointmentManager.getCustomerAppointmentHistory(
                ownerEmail,
                customerEmail
        );
    }
}