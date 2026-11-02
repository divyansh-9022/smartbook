package com.smartbook.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "business_owners")
public class BusinessOwner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String businessName;

    @NotBlank
    @Column(nullable = false)
    private String ownerName;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;
    private String pendingEmail;

    @NotBlank
    @Column(nullable = false)
    private String password;

    private String phoneNumber;

    private String logoUrl;

    @Column(unique = true, length = 100)
    private String bookingSlug;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean emailVerified = true;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean emailRemindersEnabled = true;

    public BusinessOwner() {
    }

    public Long getId() {
        return id;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
    public String getBookingSlug() {
        return bookingSlug;
    }

    public void setBookingSlug(String bookingSlug) {
        this.bookingSlug = bookingSlug;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    public String getPendingEmail() {
        return pendingEmail;
    }

    public void setPendingEmail(String pendingEmail) {
        this.pendingEmail = pendingEmail;
    }
    public boolean isEmailRemindersEnabled() {
        return emailRemindersEnabled;
    }

    public void setEmailRemindersEnabled(boolean emailRemindersEnabled) {
        this.emailRemindersEnabled = emailRemindersEnabled;
    }

}