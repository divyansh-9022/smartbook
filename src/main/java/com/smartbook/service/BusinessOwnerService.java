package com.smartbook.service;

import com.smartbook.entity.BusinessOwner;
import com.smartbook.repository.BusinessOwnerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Locale;

@Service
public class BusinessOwnerService {

    private final BusinessOwnerRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AccountSecurityService accountSecurityService;

    public BusinessOwnerService(
            BusinessOwnerRepository repository,
            PasswordEncoder passwordEncoder,
            AccountSecurityService accountSecurityService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.accountSecurityService = accountSecurityService;
    }

    public BusinessOwner registerBusinessOwner(
            String businessName,
            String ownerName,
            String email,
            String password,
            String phoneNumber) {

        if (repository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "An account with this email already exists."
            );
        }

        BusinessOwner owner = new BusinessOwner();

        owner.setBusinessName(businessName);
        owner.setOwnerName(ownerName);
        owner.setEmail(email);
        owner.setPassword(passwordEncoder.encode(password));
        owner.setPhoneNumber(phoneNumber);

        BusinessOwner savedOwner = repository.save(owner);

        accountSecurityService.sendVerificationEmail(savedOwner);

        return savedOwner;
    }
    public BusinessOwner getBusinessOwnerByEmail(String email) {

        BusinessOwner owner = repository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("Business owner not found"));

        if (owner.getBookingSlug() == null ||
                owner.getBookingSlug().isBlank()) {

            owner.setBookingSlug(
                    createUniqueBookingSlug(owner.getBusinessName())
            );

            return repository.save(owner);
        }

        return owner;
    }
    public BusinessOwner updateBusinessOwnerLogo(
            String email,
            String logoUrl
    ) {
        BusinessOwner owner = getBusinessOwnerByEmail(email);

        owner.setLogoUrl(logoUrl);

        return repository.save(owner);
    }
    public BusinessOwner updateBusinessOwnerProfile(
            String email,
            String businessName,
            String ownerName,
            String phoneNumber
    ) {
        BusinessOwner owner = getBusinessOwnerByEmail(email);

        owner.setBusinessName(businessName.trim());
        owner.setOwnerName(ownerName.trim());
        owner.setPhoneNumber(phoneNumber);
        owner.setBookingSlug(createUniqueBookingSlug(businessName));
        owner.setEmailVerified(false);

        return repository.save(owner);
    }
    public BusinessOwner updateEmailRemindersEnabled(
            String email,
            boolean enabled
    ) {
        BusinessOwner owner = getBusinessOwnerByEmail(email);

        owner.setEmailRemindersEnabled(enabled);

        return repository.save(owner);
    }
    private String createUniqueBookingSlug(String businessName) {

        String baseSlug = businessName
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");

        if (baseSlug.isBlank()) {
            baseSlug = "business";
        }

        String candidate = baseSlug;
        int suffix = 2;

        while (repository.existsByBookingSlug(candidate)) {
            candidate = baseSlug + "-" + suffix++;
        }

        return candidate;
    }
}