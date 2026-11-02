
package com.smartbook.controller;

import com.smartbook.dto.BusinessHoursRequest;
import com.smartbook.dto.BusinessHoursResponse;
import com.smartbook.service.BusinessHoursManager;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/business-hours")
public class BusinessHoursController {

    private final BusinessHoursManager businessHoursManager;

    public BusinessHoursController(
            BusinessHoursManager businessHoursManager) {

        this.businessHoursManager = businessHoursManager;
    }

    @GetMapping
    public List<BusinessHoursResponse> getMyBusinessHours(
            @AuthenticationPrincipal Jwt jwt) {

        return businessHoursManager.getMyBusinessHours(
                jwt.getSubject()
        );
    }

    @PutMapping
    public BusinessHoursResponse updateBusinessHours(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody BusinessHoursRequest request) {

        return businessHoursManager.updateBusinessHours(
                jwt.getSubject(),
                request
        );
    }
}
