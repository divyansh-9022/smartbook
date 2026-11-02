package com.smartbook.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "license_activations")
public class LicenseActivation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "business_owner_id",
            nullable = false,
            unique = true
    )
    private BusinessOwner businessOwner;

    @Column(nullable = false, unique = true, length = 64)
    private String licenseKeyHash;

    private String purchaserEmail;

    @Column(nullable = false)
    private LocalDateTime activatedAt;

    public LicenseActivation() {
    }

    @PrePersist
    protected void onCreate() {
        activatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public BusinessOwner getBusinessOwner() {
        return businessOwner;
    }

    public void setBusinessOwner(BusinessOwner businessOwner) {
        this.businessOwner = businessOwner;
    }

    public String getLicenseKeyHash() {
        return licenseKeyHash;
    }

    public void setLicenseKeyHash(String licenseKeyHash) {
        this.licenseKeyHash = licenseKeyHash;
    }

    public String getPurchaserEmail() {
        return purchaserEmail;
    }

    public void setPurchaserEmail(String purchaserEmail) {
        this.purchaserEmail = purchaserEmail;
    }

    public LocalDateTime getActivatedAt() {
        return activatedAt;
    }
}
