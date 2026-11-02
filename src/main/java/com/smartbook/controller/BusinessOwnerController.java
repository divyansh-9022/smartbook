package com.smartbook.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import com.smartbook.entity.BusinessOwner;
import com.smartbook.service.BusinessOwnerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.smartbook.service.LogoStorageService;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import com.smartbook.service.AccountSecurityService;

@RestController
@RequestMapping("/api/business-owners")
public class BusinessOwnerController {

    private final BusinessOwnerService service;

    private final LogoStorageService logoStorageService;

    private final AccountSecurityService accountSecurityService;

    public BusinessOwnerController(
            BusinessOwnerService service,
            LogoStorageService logoStorageService,
            AccountSecurityService accountSecurityService
    ) {
        this.service = service;
        this.logoStorageService = logoStorageService;
        this.accountSecurityService = accountSecurityService;
    }


    public record RegistrationRequest(
            @NotBlank String businessName,
            @NotBlank String ownerName,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            String phoneNumber
    ) {}

    public record RegistrationResponse(
            Long id,
            String businessName,
            String ownerName,
            String email,
            String phoneNumber,
            String logoUrl,
            String bookingSlug

    ) {}
    public record UpdateProfileRequest(
            @NotBlank String businessName,
            @NotBlank String ownerName,
            String phoneNumber
    ) {}

    public record UpdateEmailRequest(
            @NotBlank @Email String newEmail
    ) {}

    public record NotificationSettingsResponse(
            boolean emailRemindersEnabled
    ) {}

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse register(
            @Valid @RequestBody RegistrationRequest request) {

        BusinessOwner owner = service.registerBusinessOwner(
                request.businessName(),
                request.ownerName(),
                request.email(),
                request.password(),
                request.phoneNumber()
        );

        return new RegistrationResponse(
                owner.getId(),
                owner.getBusinessName(),
                owner.getOwnerName(),
                owner.getEmail(),
                owner.getPhoneNumber(),
                owner.getLogoUrl(),
                owner.getBookingSlug()
        );
    }
    @GetMapping("/me")
    public RegistrationResponse getMyProfile(
            @AuthenticationPrincipal Jwt jwt) {

        BusinessOwner owner =
                service.getBusinessOwnerByEmail(jwt.getSubject());

        return new RegistrationResponse(
                owner.getId(),
                owner.getBusinessName(),
                owner.getOwnerName(),
                owner.getEmail(),
                owner.getPhoneNumber(),
                owner.getLogoUrl(),
                owner.getBookingSlug()
        );
    }
    @PutMapping("/me")
    public RegistrationResponse updateMyProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        BusinessOwner owner = service.updateBusinessOwnerProfile(
                jwt.getSubject(),
                request.businessName(),
                request.ownerName(),
                request.phoneNumber()
        );

        return new RegistrationResponse(
                owner.getId(),
                owner.getBusinessName(),
                owner.getOwnerName(),
                owner.getEmail(),
                owner.getPhoneNumber(),
                owner.getLogoUrl(),
                owner.getBookingSlug()
        );
    }
    @PostMapping(
            value = "/me/logo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Map<String, String> uploadMyLogo(
            @AuthenticationPrincipal Jwt jwt,
            @RequestPart("file") MultipartFile file
    ) throws IOException {

        String logoUrl = logoStorageService.storeLogo(file);

        BusinessOwner owner = service.updateBusinessOwnerLogo(
                jwt.getSubject(),
                logoUrl
        );

        return Map.of("logoUrl", owner.getLogoUrl());
    }

    @PutMapping("/me/email")
    public Map<String, String> requestEmailChange(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateEmailRequest request
    ) {
        accountSecurityService.requestEmailChange(
                jwt.getSubject(),
                request.newEmail()
        );

        return Map.of(
                "message",
                "A verification link has been sent to your new email address."
        );
    }
    @GetMapping("/me/notification-settings")
    public NotificationSettingsResponse getNotificationSettings(
            @AuthenticationPrincipal Jwt jwt
    ) {
        BusinessOwner owner =
                service.getBusinessOwnerByEmail(jwt.getSubject());

        return new NotificationSettingsResponse(
                owner.isEmailRemindersEnabled()
        );
    }

    @PutMapping("/me/notification-settings")
    public NotificationSettingsResponse updateNotificationSettings(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody NotificationSettingsResponse request
    ) {
        BusinessOwner owner = service.updateEmailRemindersEnabled(
                jwt.getSubject(),
                request.emailRemindersEnabled()
        );

        return new NotificationSettingsResponse(
                owner.isEmailRemindersEnabled()
        );
    }
}