package com.smartbook.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AppointmentRequest(

        @NotNull
        Long serviceId,

        @NotBlank
        String customerName,

        @NotBlank
        @Email
        String customerEmail,

        String customerPhone,

        @NotNull
        @Future
        LocalDateTime appointmentDateTime

) {
}
