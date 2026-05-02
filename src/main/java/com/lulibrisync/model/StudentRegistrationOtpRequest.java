package com.lulibrisync.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_registration_otp_requests")
public class StudentRegistrationOtpRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pending_first_name", nullable = false, length = 50)
    private String pendingFirstName;

    @Column(name = "pending_middle_name", length = 50)
    private String pendingMiddleName;

    @Column(name = "pending_last_name", nullable = false, length = 50)
    private String pendingLastName;

    @Column(name = "pending_full_name", nullable = false, length = 100)
    private String pendingFullName;

    @Column(name = "pending_program", nullable = false, length = 120)
    private String pendingProgram;

    @Column(name = "pending_year_level", nullable = false, length = 60)
    private String pendingYearLevel;

    @Column(name = "pending_email", nullable = false, length = 120)
    private String pendingEmail;

    @Column(name = "pending_contact_number", nullable = false, length = 30)
    private String pendingContactNumber;

    @Column(name = "pending_birth_date", nullable = false)
    private LocalDate pendingBirthDate;

    @Column(name = "pending_province", nullable = false, length = 120)
    private String pendingProvince;

    @Column(name = "pending_city_municipality", nullable = false, length = 120)
    private String pendingCityMunicipality;

    @Column(name = "pending_barangay", nullable = false, length = 120)
    private String pendingBarangay;

    @Column(name = "pending_street", nullable = false, length = 180)
    private String pendingStreet;

    @Column(name = "pending_zipcode", nullable = false, length = 4)
    private String pendingZipcode;

    @Column(name = "pending_address", nullable = false, length = 255)
    private String pendingAddress;

    @Column(name = "pending_password_hash", nullable = false, length = 255)
    private String pendingPasswordHash;

    @Column(name = "otp_hash", nullable = false, length = 128)
    private String otpHash;

    @Column(name = "destination_email", nullable = false, length = 120)
    private String destinationEmail;

    @Column(name = "last_sent_at", nullable = false)
    private LocalDateTime lastSentAt;

    @Column(name = "resend_available_at", nullable = false)
    private LocalDateTime resendAvailableAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPendingFirstName() {
        return pendingFirstName;
    }

    public void setPendingFirstName(String pendingFirstName) {
        this.pendingFirstName = pendingFirstName;
    }

    public String getPendingMiddleName() {
        return pendingMiddleName;
    }

    public void setPendingMiddleName(String pendingMiddleName) {
        this.pendingMiddleName = pendingMiddleName;
    }

    public String getPendingLastName() {
        return pendingLastName;
    }

    public void setPendingLastName(String pendingLastName) {
        this.pendingLastName = pendingLastName;
    }

    public String getPendingFullName() {
        return pendingFullName;
    }

    public void setPendingFullName(String pendingFullName) {
        this.pendingFullName = pendingFullName;
    }

    public String getPendingProgram() {
        return pendingProgram;
    }

    public void setPendingProgram(String pendingProgram) {
        this.pendingProgram = pendingProgram;
    }

    public String getPendingYearLevel() {
        return pendingYearLevel;
    }

    public void setPendingYearLevel(String pendingYearLevel) {
        this.pendingYearLevel = pendingYearLevel;
    }

    public String getPendingEmail() {
        return pendingEmail;
    }

    public void setPendingEmail(String pendingEmail) {
        this.pendingEmail = pendingEmail;
    }

    public String getPendingContactNumber() {
        return pendingContactNumber;
    }

    public void setPendingContactNumber(String pendingContactNumber) {
        this.pendingContactNumber = pendingContactNumber;
    }

    public LocalDate getPendingBirthDate() {
        return pendingBirthDate;
    }

    public void setPendingBirthDate(LocalDate pendingBirthDate) {
        this.pendingBirthDate = pendingBirthDate;
    }

    public String getPendingProvince() {
        return pendingProvince;
    }

    public void setPendingProvince(String pendingProvince) {
        this.pendingProvince = pendingProvince;
    }

    public String getPendingCityMunicipality() {
        return pendingCityMunicipality;
    }

    public void setPendingCityMunicipality(String pendingCityMunicipality) {
        this.pendingCityMunicipality = pendingCityMunicipality;
    }

    public String getPendingBarangay() {
        return pendingBarangay;
    }

    public void setPendingBarangay(String pendingBarangay) {
        this.pendingBarangay = pendingBarangay;
    }

    public String getPendingStreet() {
        return pendingStreet;
    }

    public void setPendingStreet(String pendingStreet) {
        this.pendingStreet = pendingStreet;
    }

    public String getPendingZipcode() {
        return pendingZipcode;
    }

    public void setPendingZipcode(String pendingZipcode) {
        this.pendingZipcode = pendingZipcode;
    }

    public String getPendingAddress() {
        return pendingAddress;
    }

    public void setPendingAddress(String pendingAddress) {
        this.pendingAddress = pendingAddress;
    }

    public String getPendingPasswordHash() {
        return pendingPasswordHash;
    }

    public void setPendingPasswordHash(String pendingPasswordHash) {
        this.pendingPasswordHash = pendingPasswordHash;
    }

    public String getOtpHash() {
        return otpHash;
    }

    public void setOtpHash(String otpHash) {
        this.otpHash = otpHash;
    }

    public String getDestinationEmail() {
        return destinationEmail;
    }

    public void setDestinationEmail(String destinationEmail) {
        this.destinationEmail = destinationEmail;
    }

    public LocalDateTime getLastSentAt() {
        return lastSentAt;
    }

    public void setLastSentAt(LocalDateTime lastSentAt) {
        this.lastSentAt = lastSentAt;
    }

    public LocalDateTime getResendAvailableAt() {
        return resendAvailableAt;
    }

    public void setResendAvailableAt(LocalDateTime resendAvailableAt) {
        this.resendAvailableAt = resendAvailableAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
