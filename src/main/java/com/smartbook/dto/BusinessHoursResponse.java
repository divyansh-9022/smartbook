
package com.smartbook.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record BusinessHoursResponse(

        Long id,

        DayOfWeek dayOfWeek,

        LocalTime openTime,

        LocalTime closeTime,

        boolean closed

) {
}
