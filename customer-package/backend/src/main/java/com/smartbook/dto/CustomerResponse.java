package com.smartbook.dto;

import java.time.LocalDateTime;

public record CustomerResponse(

        Long id,
        String customerName,
        String customerEmail,
        String customerPhone,
        LocalDateTime createdAt

) {
}