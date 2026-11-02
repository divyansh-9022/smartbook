package com.smartbook.controller;

import com.smartbook.dto.BusinessHolidayRequest;
import com.smartbook.dto.BusinessHolidayResponse;
import com.smartbook.service.BusinessHolidayManager;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/business-holidays")
public class BusinessHolidayController {

    private final BusinessHolidayManager holidayManager;

    public BusinessHolidayController(
            BusinessHolidayManager holidayManager
    ) {
        this.holidayManager = holidayManager;
    }

    @GetMapping
    public ResponseEntity<List<BusinessHolidayResponse>> getHolidays(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                holidayManager.getHolidays(authentication.getName())
        );
    }

    @PostMapping
    public ResponseEntity<BusinessHolidayResponse> addHoliday(
            Authentication authentication,
            @Valid @RequestBody BusinessHolidayRequest request
    ) {
        return ResponseEntity.ok(
                holidayManager.addHoliday(
                        authentication.getName(),
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHoliday(
            Authentication authentication,
            @PathVariable Long id
    ) {
        holidayManager.deleteHoliday(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
