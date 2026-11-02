package com.smartbook.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record BusinessBreakResponse(
        Long id,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        String breakName
) {
}