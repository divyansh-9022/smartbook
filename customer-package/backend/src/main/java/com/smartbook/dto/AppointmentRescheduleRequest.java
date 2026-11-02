package com.smartbook.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AppointmentRescheduleRequest(

        @NotNull
        @Future
        LocalDateTime appointmentDateTime

) {
}