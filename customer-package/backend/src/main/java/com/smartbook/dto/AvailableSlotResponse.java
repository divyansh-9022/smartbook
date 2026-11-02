
package com.smartbook.dto;

import java.time.LocalDateTime;

public record AvailableSlotResponse(
        LocalDateTime appointmentDateTime
) {
}
