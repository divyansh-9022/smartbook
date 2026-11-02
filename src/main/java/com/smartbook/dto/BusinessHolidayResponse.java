package com.smartbook.dto;

import java.time.LocalDate;

public record BusinessHolidayResponse(
        Long id,
        LocalDate holidayDate,
        String name
) {
}
