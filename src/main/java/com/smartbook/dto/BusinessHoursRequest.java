
package com.smartbook.dto;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record BusinessHoursRequest(

        @NotNull
        DayOfWeek dayOfWeek,

        LocalTime openTime,

        LocalTime closeTime,

        boolean closed

) {
}
