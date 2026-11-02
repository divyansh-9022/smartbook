package com.smartbook.service;

import com.smartbook.entity.BusinessOwner;
import com.smartbook.entity.LicenseActivation;
import com.smartbook.repository.BusinessOwnerRepository;
import com.smartbook.repository.LicenseActivationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class LicenseActivationManager {

    private final LicenseActivationRepository activationRepository;
    private final BusinessOwnerRepository ownerRepository;
    private final GumroadLicenseService gumroadLicenseService;

    public LicenseActivationManager(
            LicenseActivationRepository activationRepository,
            BusinessOwnerRepository ownerRepository,
            GumroadLicenseService gumroadLicenseService
    ) {
        this.activationRepository = activationRepository;
        this.ownerRepository = ownerRepository;
        this.gumroadLicenseService = gumroadLicenseService;
    }

    @Transactional
    public String activateLicense(String email, String licenseKey) {

        BusinessOwner owner = ownerRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("Business owner not found."));

        if (activationRepository.findByBusinessOwner(owner).isPresent()) {
            throw new IllegalArgumentException(
                    "This SmartBook account already has an activated licence."
            );
        }

        String keyHash = hashLicenseKey(licenseKey);

        if (activationRepository.existsByLicenseKeyHash(keyHash)) {
            throw new IllegalArgumentException(
                    "This licence key has already been activated in this installation."
            );
        }

        boolean valid = gumroadLicenseService.activateLicenseKey(licenseKey);

        if (!valid) {
            throw new IllegalArgumentException(
                    "Invalid Gumroad licence key or unverified purchase."
            );
        }

        LicenseActivation activation = new LicenseActivation();
        activation.setBusinessOwner(owner);
        activation.setLicenseKeyHash(keyHash);

        activationRepository.save(activation);

        return "SmartBook licence activated successfully!";
    }

    @Transactional(readOnly = true)
    public boolean isActivated(String email) {

        BusinessOwner owner = ownerRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("Business owner not found."));

        return activationRepository.findByBusinessOwner(owner).isPresent();
    }

    private String hashLicenseKey(String licenseKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    licenseKey.trim().getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "Unable to process licence key.",
                    exception
            );
        }
    }
}