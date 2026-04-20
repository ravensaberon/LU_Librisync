package com.lulibrisync.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class StudentProfileOtpState implements Serializable {

    private StudentProfileUpdateRequest updateRequest;
    private String otpCode;
    private LocalDateTime expiresAt;
    private String destinationEmail;

    public StudentProfileOtpState() {
    }

    public StudentProfileOtpState(StudentProfileUpdateRequest updateRequest,
                                  String otpCode,
                                  LocalDateTime expiresAt,
                                  String destinationEmail) {
        this.updateRequest = updateRequest;
        this.otpCode = otpCode;
        this.expiresAt = expiresAt;
        this.destinationEmail = destinationEmail;
    }

    public StudentProfileUpdateRequest getUpdateRequest() {
        return updateRequest;
    }

    public void setUpdateRequest(StudentProfileUpdateRequest updateRequest) {
        this.updateRequest = updateRequest;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getDestinationEmail() {
        return destinationEmail;
    }

    public void setDestinationEmail(String destinationEmail) {
        this.destinationEmail = destinationEmail;
    }
}
