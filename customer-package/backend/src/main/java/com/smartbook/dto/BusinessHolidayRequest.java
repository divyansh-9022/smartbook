package com.smartbook.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record BusinessHolidayRequest(

        @NotNull
        @FutureOrPresent
        LocalDate holidayDate,

        @NotBlank
        @Size(max = 100)
        String name

) {
}