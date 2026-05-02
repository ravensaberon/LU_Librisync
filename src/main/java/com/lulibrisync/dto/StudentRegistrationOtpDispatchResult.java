package com.lulibrisync.dto;

public class StudentRegistrationOtpDispatchResult {

    private final StudentRegistrationOtpState otpState;
    private final boolean sent;
    private final boolean delivered;
    private final boolean cooldownActive;

    public StudentRegistrationOtpDispatchResult(StudentRegistrationOtpState otpState,
                                                boolean sent,
                                                boolean delivered,
                                                boolean cooldownActive) {
        this.otpState = otpState;
        this.sent = sent;
        this.delivered = delivered;
        this.cooldownActive = cooldownActive;
    }

    public StudentRegistrationOtpState getOtpState() {
        return otpState;
    }

    public boolean isSent() {
        return sent;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public boolean isCooldownActive() {
        return cooldownActive;
    }
}
