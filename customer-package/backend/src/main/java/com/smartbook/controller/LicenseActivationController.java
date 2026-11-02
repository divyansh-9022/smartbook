package com.smartbook.controller;

import com.smartbook.service.LicenseActivationManager;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/license")
public class LicenseActivationController {

    private final LicenseActivationManager licenseManager;
    private final boolean enforcementEnabled;

    public LicenseActivationController(
            LicenseActivationManager licenseManager,
            @Value("${app.license.enforcement-enabled:false}")
            boolean enforcementEnabled
    ) {
        this.licenseManager = licenseManager;
        this.enforcementEnabled = enforcementEnabled;
    }

    public record ActivationRequest(
            @NotBlank
            @Size(max = 255)
            String licenseKey
    ) {}

    public record ActivationResponse(
            boolean activated,
            boolean enforcementEnabled,
            String message
    ) {}

    @GetMapping("/status")
    public ResponseEntity<ActivationResponse> getLicenseStatus(
            Authentication authentication
    ) {
        boolean activated = licenseManager.isActivated(
                authentication.getName()
        );

        return ResponseEntity.ok(
                new ActivationResponse(
                        activated,
                        enforcementEnabled,
                        activated
                                ? "SmartBook licence is active."
                                : "Please activate your SmartBook licence."
                )
        );
    }

    @PostMapping("/activate")
    public ResponseEntity<ActivationResponse> activateLicense(
            Authentication authentication,
            @Valid @RequestBody ActivationRequest request
    ) {
        String message = licenseManager.activateLicense(
                authentication.getName(),
                request.licenseKey()
        );

        return ResponseEntity.ok(
                new ActivationResponse(true, enforcementEnabled, message)
        );
    }
}