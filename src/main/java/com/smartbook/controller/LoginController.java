package com.smartbook.controller;

import com.smartbook.service.LoginService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import com.smartbook.service.AccountSecurityService;
import jakarta.validation.constraints.Size;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final LoginService loginService;

    private final AccountSecurityService accountSecurityService;

    public LoginController(
            LoginService loginService,
            AccountSecurityService accountSecurityService
    ) {
        this.loginService = loginService;
        this.accountSecurityService = accountSecurityService;
    }


    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    public record LoginResponse(String token, String tokenType) {}

    public record ForgotPasswordRequest(
            @NotBlank @Email String email
    ) {}

    public record ResetPasswordRequest(
            @NotBlank String token,
            @NotBlank @Size(min = 8, max = 72) String newPassword
    ) {}
    public record VerifyEmailRequest(
            @NotBlank String token
    ) {}

    @PostMapping("/verify-email-change")
    public Map<String, String> verifyEmailChange(
            @Valid @RequestBody VerifyEmailRequest request
    ) {
        accountSecurityService.verifyEmailChange(request.token());

        return Map.of(
                "message",
                "Your login email has been updated successfully."
        );
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request) {

        String token = loginService.login(
                request.email(),
                request.password()
        );

        return new LoginResponse(token, "Bearer");
    }
    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        accountSecurityService.requestPasswordReset(request.email());

        return Map.of(
                "message",
                "If an account exists for that email, a password reset link has been sent."
        );
    }
    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        accountSecurityService.resetPassword(
                request.token(),
                request.newPassword()
        );

        return Map.of(
                "message",
                "Your password has been reset successfully."
        );
    }
    @PostMapping("/verify-email")
    public Map<String, String> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request
    ) {
        accountSecurityService.verifyEmail(request.token());

        return Map.of(
                "message",
                "Your email has been verified successfully."
        );
    }
}