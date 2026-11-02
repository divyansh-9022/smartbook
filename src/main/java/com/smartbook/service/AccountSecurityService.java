package com.smartbook.service;

import com.smartbook.entity.AccountToken;
import com.smartbook.entity.BusinessOwner;
import com.smartbook.repository.AccountTokenRepository;
import com.smartbook.repository.BusinessOwnerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class AccountSecurityService {

    private static final String EMAIL_VERIFICATION = "EMAIL_VERIFICATION";
    private static final String PASSWORD_RESET = "PASSWORD_RESET";
    private static final String EMAIL_CHANGE = "EMAIL_CHANGE";

    private final AccountTokenRepository tokenRepository;
    private final BusinessOwnerRepository ownerRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public AccountSecurityService(
            AccountTokenRepository tokenRepository,
            BusinessOwnerRepository ownerRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder
    ) {
        this.tokenRepository = tokenRepository;
        this.ownerRepository = ownerRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void sendVerificationEmail(BusinessOwner owner) {

        String rawToken = createToken(owner, EMAIL_VERIFICATION,
                Duration.ofHours(24));

        String verificationUrl =
                frontendUrl + "/verify-email?token=" + rawToken;

        emailService.sendVerificationEmail(
                owner.getEmail(),
                verificationUrl
        );
    }

    @Transactional
    public void requestEmailChange(String currentEmail, String newEmail) {

        BusinessOwner owner = ownerRepository.findByEmail(currentEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("Business owner not found."));

        String normalizedEmail = newEmail.trim().toLowerCase(Locale.ROOT);

        if (normalizedEmail.equalsIgnoreCase(owner.getEmail())) {
            throw new IllegalArgumentException(
                    "This is already your registered email address."
            );
        }

        if (ownerRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException(
                    "This email address is already registered."
            );
        }

        owner.setPendingEmail(normalizedEmail);
        ownerRepository.save(owner);

        String rawToken = createToken(
                owner,
                EMAIL_CHANGE,
                Duration.ofHours(24)
        );

        String verificationUrl =
                frontendUrl + "/verify-email?token=" + rawToken
                        + "&type=email-change";

        emailService.sendVerificationEmail(
                normalizedEmail,
                verificationUrl
        );
    }

    @Transactional
    public void requestPasswordReset(String email) {

        var ownerOptional = ownerRepository.findByEmail(email.trim());

        if (ownerOptional.isEmpty()) {
            System.out.println(
                    "SMARTBOOK: Password reset requested, but no matching account was found."
            );
            return;
        }

        BusinessOwner owner = ownerOptional.get();

        try {
            String rawToken = createToken(
                    owner,
                    PASSWORD_RESET,
                    Duration.ofMinutes(30)
            );

            String resetUrl =
                    frontendUrl + "/reset-password?token=" + rawToken;

            emailService.sendPasswordResetEmail(
                    owner.getEmail(),
                    resetUrl
            );

            System.out.println(
                    "SMARTBOOK: Password reset email submitted to mail server."
            );

        } catch (Exception exception) {
            System.err.println(
                    "SMARTBOOK: Password reset email could not be sent."
            );
            exception.printStackTrace();
        }
    }

    @Transactional
    public void verifyEmail(String rawToken) {

        AccountToken token = findValidToken(
                rawToken,
                EMAIL_VERIFICATION
        );

        token.setUsed(true);
        token.getBusinessOwner().setEmailVerified(true);

        ownerRepository.save(token.getBusinessOwner());
        tokenRepository.save(token);
    }

    @Transactional
    public void verifyEmailChange(String rawToken) {

        AccountToken token = findValidToken(
                rawToken,
                EMAIL_CHANGE
        );

        BusinessOwner owner = token.getBusinessOwner();

        String newEmail = owner.getPendingEmail();

        if (newEmail == null || newEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "No pending email change was found."
            );
        }

        if (ownerRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException(
                    "This email address is already registered."
            );
        }

        owner.setEmail(newEmail);
        owner.setPendingEmail(null);
        owner.setEmailVerified(true);

        token.setUsed(true);

        ownerRepository.save(owner);
        tokenRepository.save(token);
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {

        AccountToken token = findValidToken(
                rawToken,
                PASSWORD_RESET
        );

        token.getBusinessOwner().setPassword(
                passwordEncoder.encode(newPassword)
        );

        token.setUsed(true);

        ownerRepository.save(token.getBusinessOwner());
        tokenRepository.save(token);
    }

    private String createToken(
            BusinessOwner owner,
            String tokenType,
            Duration validity
    ) {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        AccountToken token = new AccountToken();
        token.setBusinessOwner(owner);
        token.setTokenHash(hashToken(rawToken));
        token.setTokenType(tokenType);
        token.setExpiresAt(LocalDateTime.now().plus(validity));
        token.setUsed(false);

        tokenRepository.save(token);

        return rawToken;
    }

    private AccountToken findValidToken(
            String rawToken,
            String tokenType
    ) {
        AccountToken token = tokenRepository
                .findByTokenHashAndTokenTypeAndUsedFalse(
                        hashToken(rawToken),
                        tokenType
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid or already used token."
                        ));

        if (!token.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "This link has expired. Please request a new one."
            );
        }

        return token;
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            return HexFormat.of().formatHex(
                    digest.digest(rawToken.getBytes(
                            java.nio.charset.StandardCharsets.UTF_8
                    ))
            );

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to process security token.",
                    exception
            );
        }
    }
}