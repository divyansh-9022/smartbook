
package com.smartbook.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AppointmentResponse(

        Long id,
        String serviceName,
        BigDecimal price,
        Integer durationMinutes,
        String customerName,
        String customerEmail,
        String customerPhone,
        LocalDateTime appointmentDateTime,
        String status,
        boolean reminderSent

) {
}
