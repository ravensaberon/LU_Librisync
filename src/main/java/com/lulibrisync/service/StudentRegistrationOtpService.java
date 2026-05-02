package com.lulibrisync.service;

import com.lulibrisync.dto.PreparedStudentRegistration;
import com.lulibrisync.dto.StudentRegistrationForm;
import com.lulibrisync.dto.StudentRegistrationOtpDispatchResult;
import com.lulibrisync.dto.StudentRegistrationOtpState;
import com.lulibrisync.model.StudentRegistrationOtpRequest;
import com.lulibrisync.repository.StudentRegistrationOtpRequestRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

@Service
public class StudentRegistrationOtpService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a", Locale.ENGLISH);

    private final StudentRegistrationOtpRequestRepository otpRequestRepository;
    private final AuthService authService;
    private final EmailNotificationService emailNotificationService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final int resendCooldownSeconds;
    private final int otpValidityMinutes;

    public StudentRegistrationOtpService(StudentRegistrationOtpRequestRepository otpRequestRepository,
                                         AuthService authService,
                                         EmailNotificationService emailNotificationService,
                                         @Value("${lulibrisync.registration-otp.resend-seconds:180}") int resendCooldownSeconds,
                                         @Value("${lulibrisync.registration-otp.validity-minutes:10}") int otpValidityMinutes) {
        this.otpRequestRepository = otpRequestRepository;
        this.authService = authService;
        this.emailNotificationService = emailNotificationService;
        this.resendCooldownSeconds = Math.max(30, resendCooldownSeconds);
        this.otpValidityMinutes = Math.max(3, otpValidityMinutes);
    }

    public StudentRegistrationOtpState getActiveOtpState(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return otpRequestRepository.findFirstByPendingEmailIgnoreCaseAndUsedFalseOrderByCreatedAtDesc(email.trim())
                .filter(request -> request.getExpiresAt() != null && request.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(this::toOtpState)
                .orElse(null);
    }

    public StudentRegistrationOtpState getLatestOtpState(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return otpRequestRepository.findFirstByPendingEmailIgnoreCaseAndUsedFalseOrderByCreatedAtDesc(email.trim())
                .map(this::toOtpState)
                .orElse(null);
    }

    @Transactional
    public StudentRegistrationOtpDispatchResult requestOtp(StudentRegistrationForm form) {
        PreparedStudentRegistration preparedRegistration = authService.prepareStudentRegistration(form);
        StudentRegistrationOtpRequest otpRequest = otpRequestRepository
                .findFirstByPendingEmailIgnoreCaseAndUsedFalseOrderByCreatedAtDesc(preparedRegistration.getEmail())
                .orElseGet(StudentRegistrationOtpRequest::new);

        applyPendingRegistration(otpRequest, preparedRegistration);
        otpRequest.setDestinationEmail(preparedRegistration.getEmail());
        return sendOrRefreshOtp(otpRequest);
    }

    @Transactional
    public StudentRegistrationOtpDispatchResult resendOtp(String email) {
        StudentRegistrationOtpRequest latestRequest = otpRequestRepository
                .findFirstByPendingEmailIgnoreCaseAndUsedFalseOrderByCreatedAtDesc(email == null ? "" : email.trim())
                .orElse(null);
        if (latestRequest == null) {
            throw new IllegalArgumentException("Request a registration OTP first.");
        }
        if (latestRequest.getExpiresAt() == null || !latestRequest.getExpiresAt().isAfter(LocalDateTime.now())) {
            expireOutstandingRequests(latestRequest.getPendingEmail());
            throw new IllegalArgumentException("The last OTP has expired. Request a new registration OTP.");
        }
        return sendOrRefreshOtp(latestRequest);
    }

    @Transactional
    public void verifyOtp(String email, String otpCode) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email address is required.");
        }
        if (otpCode == null || otpCode.trim().isBlank()) {
            throw new IllegalArgumentException("Enter the 6-digit OTP.");
        }

        StudentRegistrationOtpRequest otpRequest = otpRequestRepository
                .findFirstByPendingEmailIgnoreCaseAndUsedFalseOrderByCreatedAtDesc(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("Request a registration OTP first."));

        LocalDateTime now = LocalDateTime.now();
        if (otpRequest.getExpiresAt() == null || !otpRequest.getExpiresAt().isAfter(now)) {
            throw new IllegalArgumentException("The OTP has expired. Request a new code to continue.");
        }
        if (!hashOtp(otpCode.trim()).equals(otpRequest.getOtpHash())) {
            throw new IllegalArgumentException("Invalid OTP. Please try again.");
        }

        authService.createStudentFromPreparedRegistration(toPreparedRegistration(otpRequest));

        otpRequest.setUsed(true);
        otpRequest.setVerifiedAt(now);
        otpRequestRepository.save(otpRequest);
        expireOutstandingRequests(otpRequest.getPendingEmail());
    }

    private void applyPendingRegistration(StudentRegistrationOtpRequest otpRequest,
                                          PreparedStudentRegistration preparedRegistration) {
        otpRequest.setPendingFirstName(preparedRegistration.getFirstName());
        otpRequest.setPendingMiddleName(preparedRegistration.getMiddleName());
        otpRequest.setPendingLastName(preparedRegistration.getLastName());
        otpRequest.setPendingFullName(preparedRegistration.getFullName());
        otpRequest.setPendingProgram(preparedRegistration.getProgram());
        otpRequest.setPendingYearLevel(preparedRegistration.getYearLevel());
        otpRequest.setPendingEmail(preparedRegistration.getEmail());
        otpRequest.setPendingContactNumber(preparedRegistration.getContactNumber());
        otpRequest.setPendingBirthDate(preparedRegistration.getBirthDate());
        otpRequest.setPendingProvince(preparedRegistration.getProvince());
        otpRequest.setPendingCityMunicipality(preparedRegistration.getCityMunicipality());
        otpRequest.setPendingBarangay(preparedRegistration.getBarangay());
        otpRequest.setPendingStreet(preparedRegistration.getStreet());
        otpRequest.setPendingZipcode(preparedRegistration.getZipcode());
        otpRequest.setPendingAddress(preparedRegistration.getAddress());
        otpRequest.setPendingPasswordHash(preparedRegistration.getPasswordHash());
    }

    private StudentRegistrationOtpDispatchResult sendOrRefreshOtp(StudentRegistrationOtpRequest otpRequest) {
        PreparedStudentRegistration preparedRegistration = toPreparedRegistration(otpRequest);
        LocalDateTime now = LocalDateTime.now();
        if (otpRequest.getId() != null
                && otpRequest.getResendAvailableAt() != null
                && otpRequest.getResendAvailableAt().isAfter(now)
                && otpRequest.getExpiresAt() != null
                && otpRequest.getExpiresAt().isAfter(now)) {
            StudentRegistrationOtpRequest savedRequest = otpRequestRepository.save(otpRequest);
            return new StudentRegistrationOtpDispatchResult(toOtpState(savedRequest), false, false, true);
        }

        String otpCode = generateOtpCode();
        otpRequest.setOtpHash(hashOtp(otpCode));
        otpRequest.setLastSentAt(now);
        otpRequest.setResendAvailableAt(now.plusSeconds(resendCooldownSeconds));
        otpRequest.setExpiresAt(now.plusMinutes(otpValidityMinutes));
        otpRequest.setUsed(false);
        otpRequest.setVerifiedAt(null);

        StudentRegistrationOtpRequest savedRequest = otpRequestRepository.save(otpRequest);
        boolean delivered = emailNotificationService.sendImmediateHtmlEmail(
                preparedRegistration.getEmail(),
                "LU Librisync Verification Code | Student Registration",
                buildRegistrationOtpEmailBody(preparedRegistration, otpCode, savedRequest)
        );

        return new StudentRegistrationOtpDispatchResult(toOtpState(savedRequest), true, delivered, false);
    }

    private PreparedStudentRegistration toPreparedRegistration(StudentRegistrationOtpRequest otpRequest) {
        return new PreparedStudentRegistration(
                otpRequest.getPendingFirstName(),
                otpRequest.getPendingMiddleName(),
                otpRequest.getPendingLastName(),
                otpRequest.getPendingFullName(),
                otpRequest.getPendingProgram(),
                otpRequest.getPendingYearLevel(),
                otpRequest.getPendingEmail(),
                otpRequest.getPendingContactNumber(),
                otpRequest.getPendingBirthDate(),
                otpRequest.getPendingProvince(),
                otpRequest.getPendingCityMunicipality(),
                otpRequest.getPendingBarangay(),
                otpRequest.getPendingStreet(),
                otpRequest.getPendingZipcode(),
                otpRequest.getPendingAddress(),
                otpRequest.getPendingPasswordHash()
        );
    }

    private StudentRegistrationOtpState toOtpState(StudentRegistrationOtpRequest otpRequest) {
        StudentRegistrationForm registrationForm = new StudentRegistrationForm(
                otpRequest.getPendingFirstName(),
                otpRequest.getPendingMiddleName(),
                otpRequest.getPendingLastName(),
                otpRequest.getPendingProgram(),
                otpRequest.getPendingYearLevel(),
                otpRequest.getPendingEmail(),
                otpRequest.getPendingContactNumber(),
                otpRequest.getPendingBirthDate() == null ? null : otpRequest.getPendingBirthDate().toString(),
                otpRequest.getPendingProvince(),
                otpRequest.getPendingCityMunicipality(),
                otpRequest.getPendingBarangay(),
                otpRequest.getPendingStreet(),
                otpRequest.getPendingZipcode(),
                "",
                "",
                true
        );

        return new StudentRegistrationOtpState(
                registrationForm,
                otpRequest.getPendingEmail(),
                maskEmail(otpRequest.getDestinationEmail()),
                otpRequest.getExpiresAt(),
                otpRequest.getResendAvailableAt()
        );
    }

    private void expireOutstandingRequests(String email) {
        List<StudentRegistrationOtpRequest> requests = otpRequestRepository
                .findByPendingEmailIgnoreCaseAndUsedFalseOrderByCreatedAtDesc(email);
        boolean changed = false;
        for (StudentRegistrationOtpRequest request : requests) {
            if (!request.isUsed()) {
                request.setUsed(true);
                changed = true;
            }
        }
        if (changed) {
            otpRequestRepository.saveAll(requests);
        }
    }

    private String buildRegistrationOtpEmailBody(PreparedStudentRegistration registration,
                                                 String otpCode,
                                                 StudentRegistrationOtpRequest otpRequest) {
        return """
                <div style="margin:0;padding:24px;background:#f4faf6;font-family:Segoe UI,Arial,sans-serif;color:#163322;">
                  <div style="max-width:640px;margin:0 auto;background:#ffffff;border:1px solid #d5eadc;border-radius:24px;overflow:hidden;box-shadow:0 18px 44px rgba(18,77,47,0.12);">
                    <div style="padding:24px 32px;background:linear-gradient(135deg,#0f7a36,#34c66a);color:#ffffff;">
                      <div style="font-size:13px;letter-spacing:0.12em;text-transform:uppercase;opacity:0.88;">LU Librisync</div>
                      <h1 style="margin:10px 0 4px;font-size:28px;line-height:1.2;">Student Registration Verification</h1>
                      <p style="margin:0;font-size:15px;opacity:0.92;">Use this one-time verification code to finish creating your student library account.</p>
                    </div>
                    <div style="padding:32px;">
                      <p style="margin:0 0 16px;font-size:15px;line-height:1.7;">Hello %s,</p>
                      <p style="margin:0 0 20px;font-size:15px;line-height:1.7;">We received a student registration request for LU Librisync. Enter the code below on the registration page to continue. For your security, do not share this code with anyone.</p>
                      <div style="margin:0 0 24px;padding:18px 20px;border-radius:20px;background:#effaf2;border:1px solid #c9ead1;text-align:center;">
                        <div style="font-size:12px;font-weight:700;letter-spacing:0.12em;text-transform:uppercase;color:#2f7d49;">Verification Code</div>
                        <div style="margin-top:8px;font-size:34px;font-weight:800;letter-spacing:0.34em;color:#0f7a36;">%s</div>
                      </div>
                      <div style="margin:0 0 24px;padding:20px;border-radius:18px;background:#fbfefd;border:1px solid #e0efe4;">
                        <div style="font-size:15px;font-weight:700;color:#18452d;margin-bottom:12px;">Registration Details</div>
                        <table style="width:100%%;border-collapse:collapse;font-size:14px;line-height:1.6;">
                          <tr><td style="padding:6px 0;color:#5f7b69;">Student Name</td><td style="padding:6px 0;text-align:right;font-weight:600;color:#173522;">%s</td></tr>
                          <tr><td style="padding:6px 0;color:#5f7b69;">Program</td><td style="padding:6px 0;text-align:right;font-weight:600;color:#173522;">%s</td></tr>
                          <tr><td style="padding:6px 0;color:#5f7b69;">Year Level</td><td style="padding:6px 0;text-align:right;font-weight:600;color:#173522;">%s</td></tr>
                          <tr><td style="padding:6px 0;color:#5f7b69;">Registered Email</td><td style="padding:6px 0;text-align:right;font-weight:600;color:#173522;">%s</td></tr>
                          <tr><td style="padding:6px 0;color:#5f7b69;">Code Expires</td><td style="padding:6px 0;text-align:right;font-weight:600;color:#173522;">%s</td></tr>
                          <tr><td style="padding:6px 0;color:#5f7b69;">Resend Available</td><td style="padding:6px 0;text-align:right;font-weight:600;color:#173522;">%s</td></tr>
                        </table>
                      </div>
                      <div style="padding:16px 18px;border-radius:16px;background:#fff8ea;border:1px solid #f1ddb1;color:#6b5112;font-size:13px;line-height:1.7;">
                        If you did not request this student registration, you can safely ignore this email. No student account will be created unless the correct verification code is entered before it expires.
                      </div>
                    </div>
                    <div style="padding:18px 32px;background:#f6fbf7;border-top:1px solid #e1efe5;font-size:12px;line-height:1.7;color:#6c8375;">
                      This is an automated message from LU Librisync. Please do not reply to this email.
                    </div>
                  </div>
                </div>
                """.formatted(
                escapeHtml(registration.getFullName()),
                escapeHtml(otpCode),
                escapeHtml(registration.getFullName()),
                escapeHtml(registration.getProgram()),
                escapeHtml(registration.getYearLevel()),
                escapeHtml(registration.getEmail()),
                escapeHtml(DATE_TIME_FORMATTER.format(otpRequest.getExpiresAt())),
                escapeHtml(DATE_TIME_FORMATTER.format(otpRequest.getResendAvailableAt()))
        );
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "your registered email";
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return email;
        }

        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        return localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1) + domain;
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String generateOtpCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    private String hashOtp(String otpCode) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(messageDigest.digest(otpCode.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Unable to hash OTP code.", exception);
        }
    }
}
