package com.lulibrisync.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class StudentPasswordOtpState implements Serializable {

    private String destinationEmail;
    private String maskedEmail;
    private LocalDateTime expiresAt;
    private LocalDateTime resendAvailableAt;

    public StudentPasswordOtpState() {
    }

    public StudentPasswordOtpState(String destinationEmail,
                                   String maskedEmail,
                                   LocalDateTime expiresAt,
                                   LocalDateTime resendAvailableAt) {
        this.destinationEmail = destinationEmail;
        this.maskedEmail = maskedEmail;
        this.expiresAt = expiresAt;
        this.resendAvailableAt = resendAvailableAt;
    }

    public String getDestinationEmail() {
        return destinationEmail;
    }

    public void setDestinationEmail(String destinationEmail) {
        this.destinationEmail = destinationEmail;
    }

    public String getMaskedEmail() {
        return maskedEmail;
    }

    public void setMaskedEmail(String maskedEmail) {
        this.maskedEmail = maskedEmail;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getResendAvailableAt() {
        return resendAvailableAt;
    }

    public void setResendAvailableAt(LocalDateTime resendAvailableAt) {
        this.resendAvailableAt = resendAvailableAt;
    }
}
