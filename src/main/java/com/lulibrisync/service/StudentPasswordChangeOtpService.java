package com.lulibrisync.service;

import com.lulibrisync.dto.StudentPasswordOtpDispatchResult;
import com.lulibrisync.dto.StudentPasswordOtpState;
import com.lulibrisync.model.Student;
import com.lulibrisync.model.StudentPasswordChangeOtpRequest;
import com.lulibrisync.repository.StudentPasswordChangeOtpRequestRepository;
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
public class StudentPasswordChangeOtpService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a", Locale.ENGLISH);

    private final StudentPasswordChangeOtpRequestRepository otpRequestRepository;
    private final StudentService studentService;
    private final EmailNotificationService emailNotificationService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final int resendCooldownSeconds;
    private final int otpValidityMinutes;

    public StudentPasswordChangeOtpService(StudentPasswordChangeOtpRequestRepository otpRequestRepository,
                                           StudentService studentService,
                                           EmailNotificationService emailNotificationService,
                                           @Value("${lulibrisync.student-password-otp.resend-seconds:180}") int resendCooldownSeconds,
                                           @Value("${lulibrisync.student-password-otp.validity-minutes:10}") int otpValidityMinutes) {
        this.otpRequestRepository = otpRequestRepository;
        this.studentService = studentService;
        this.emailNotificationService = emailNotificationService;
        this.resendCooldownSeconds = Math.max(30, resendCooldownSeconds);
        this.otpValidityMinutes = Math.max(3, otpValidityMinutes);
    }

    public StudentPasswordOtpState getActiveOtpState(Student student) {
        return otpRequestRepository.findFirstByStudent_IdAndUsedFalseOrderByCreatedAtDesc(student.getId())
                .filter(request -> request.getExpiresAt() != null && request.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(this::toOtpState)
                .orElse(null);
    }

    public StudentPasswordOtpState getLatestOtpState(Student student) {
        return otpRequestRepository.findFirstByStudent_IdAndUsedFalseOrderByCreatedAtDesc(student.getId())
                .map(this::toOtpState)
                .orElse(null);
    }

    @Transactional
    public StudentPasswordOtpDispatchResult requestOtp(Student student,
                                                       String currentPassword,
                                                       String newPassword,
                                                       String confirmPassword) {
        String pendingPasswordHash = studentService.preparePasswordChange(
                student.getUser().getEmail(),
                currentPassword,
                newPassword,
                confirmPassword
        );
        return sendOrRefreshOtp(student, pendingPasswordHash);
    }

    @Transactional
    public StudentPasswordOtpDispatchResult resendOtp(Student student) {
        StudentPasswordChangeOtpRequest latestRequest = otpRequestRepository
                .findFirstByStudent_IdAndUsedFalseOrderByCreatedAtDesc(student.getId())
                .orElseThrow(() -> new IllegalArgumentException("Request a password change OTP first."));

        if (latestRequest.getExpiresAt() == null || !latestRequest.getExpiresAt().isAfter(LocalDateTime.now())) {
            expireOutstandingRequests(student.getId());
            throw new IllegalArgumentException("The last OTP has expired. Request a new password change OTP.");
        }

        return sendOrRefreshOtp(student, latestRequest.getPendingPasswordHash());
    }

    @Transactional
    public void verifyOtp(Student student, String otpCode) {
        if (otpCode == null || otpCode.trim().isBlank()) {
            throw new IllegalArgumentException("Enter the 6-digit OTP.");
        }

        StudentPasswordChangeOtpRequest otpRequest = otpRequestRepository
                .findFirstByStudent_IdAndUsedFalseOrderByCreatedAtDesc(student.getId())
                .orElseThrow(() -> new IllegalArgumentException("Request a password change OTP first."));

        LocalDateTime now = LocalDateTime.now();
        if (otpRequest.getExpiresAt() == null || !otpRequest.getExpiresAt().isAfter(now)) {
            throw new IllegalArgumentException("The OTP has expired. Request a new code to continue.");
        }
        if (!hashOtp(otpCode.trim()).equals(otpRequest.getOtpHash())) {
            throw new IllegalArgumentException("Invalid OTP. Please try again.");
        }

        studentService.applyEncodedPasswordChange(student.getUser().getEmail(), otpRequest.getPendingPasswordHash());

        otpRequest.setUsed(true);
        otpRequest.setVerifiedAt(now);
        otpRequestRepository.save(otpRequest);
        expireOutstandingRequests(student.getId());
    }

    private StudentPasswordOtpDispatchResult sendOrRefreshOtp(Student student, String pendingPasswordHash) {
        StudentPasswordChangeOtpRequest otpRequest = otpRequestRepository
                .findFirstByStudent_IdAndUsedFalseOrderByCreatedAtDesc(student.getId())
                .orElseGet(StudentPasswordChangeOtpRequest::new);

        otpRequest.setStudent(student);
        otpRequest.setPendingPasswordHash(pendingPasswordHash);
        otpRequest.setDestinationEmail(student.getUser().getEmail());

        LocalDateTime now = LocalDateTime.now();
        if (otpRequest.getId() != null
                && otpRequest.getResendAvailableAt() != null
                && otpRequest.getResendAvailableAt().isAfter(now)
                && otpRequest.getExpiresAt() != null
                && otpRequest.getExpiresAt().isAfter(now)) {
            StudentPasswordChangeOtpRequest savedRequest = otpRequestRepository.save(otpRequest);
            return new StudentPasswordOtpDispatchResult(toOtpState(savedRequest), false, false, true);
        }

        String otpCode = generateOtpCode();
        otpRequest.setOtpHash(hashOtp(otpCode));
        otpRequest.setLastSentAt(now);
        otpRequest.setResendAvailableAt(now.plusSeconds(resendCooldownSeconds));
        otpRequest.setExpiresAt(now.plusMinutes(otpValidityMinutes));
        otpRequest.setUsed(false);
        otpRequest.setVerifiedAt(null);

        StudentPasswordChangeOtpRequest savedRequest = otpRequestRepository.save(otpRequest);
        boolean delivered = emailNotificationService.sendImmediateHtmlEmail(
                student.getUser().getEmail(),
                "LU Librisync Verification Code | Student Password Change",
                buildPasswordChangeOtpEmailBody(student, otpCode, savedRequest)
        );

        return new StudentPasswordOtpDispatchResult(toOtpState(savedRequest), true, delivered, false);
    }

    private StudentPasswordOtpState toOtpState(StudentPasswordChangeOtpRequest otpRequest) {
        return new StudentPasswordOtpState(
                otpRequest.getDestinationEmail(),
                maskEmail(otpRequest.getDestinationEmail()),
                otpRequest.getExpiresAt(),
                otpRequest.getResendAvailableAt()
        );
    }

    private void expireOutstandingRequests(Long studentId) {
        List<StudentPasswordChangeOtpRequest> requests = otpRequestRepository
                .findByStudent_IdAndUsedFalseOrderByCreatedAtDesc(studentId);
        boolean changed = false;
        for (StudentPasswordChangeOtpRequest request : requests) {
            if (!request.isUsed()) {
                request.setUsed(true);
                changed = true;
            }
        }
        if (changed) {
            otpRequestRepository.saveAll(requests);
        }
    }

    private String buildPasswordChangeOtpEmailBody(Student student,
                                                   String otpCode,
                                                   StudentPasswordChangeOtpRequest otpRequest) {
        return """
                <div style="margin:0;padding:24px;background:#f4faf6;font-family:Segoe UI,Arial,sans-serif;color:#163322;">
                  <div style="max-width:640px;margin:0 auto;background:#ffffff;border:1px solid #d5eadc;border-radius:24px;overflow:hidden;box-shadow:0 18px 44px rgba(18,77,47,0.12);">
                    <div style="padding:24px 32px;background:linear-gradient(135deg,#0f7a36,#34c66a);color:#ffffff;">
                      <div style="font-size:13px;letter-spacing:0.12em;text-transform:uppercase;opacity:0.88;">LU Librisync</div>
                      <h1 style="margin:10px 0 4px;font-size:28px;line-height:1.2;">Student Password Change Verification</h1>
                      <p style="margin:0;font-size:15px;opacity:0.92;">Use this one-time verification code to confirm your student account password change.</p>
                    </div>
                    <div style="padding:32px;">
                      <p style="margin:0 0 16px;font-size:15px;line-height:1.7;">Hello %s,</p>
                      <p style="margin:0 0 20px;font-size:15px;line-height:1.7;">We received a password change request for your LU Librisync student account. Enter the code below on your profile page to continue. For your security, do not share this code with anyone.</p>
                      <div style="margin:0 0 24px;padding:18px 20px;border-radius:20px;background:#effaf2;border:1px solid #c9ead1;text-align:center;">
                        <div style="font-size:12px;font-weight:700;letter-spacing:0.12em;text-transform:uppercase;color:#2f7d49;">Verification Code</div>
                        <div style="margin-top:8px;font-size:34px;font-weight:800;letter-spacing:0.34em;color:#0f7a36;">%s</div>
                      </div>
                      <div style="margin:0 0 24px;padding:20px;border-radius:18px;background:#fbfefd;border:1px solid #e0efe4;">
                        <div style="font-size:15px;font-weight:700;color:#18452d;margin-bottom:12px;">Request Details</div>
                        <table style="width:100%%;border-collapse:collapse;font-size:14px;line-height:1.6;">
                          <tr><td style="padding:6px 0;color:#5f7b69;">Student ID</td><td style="padding:6px 0;text-align:right;font-weight:600;color:#173522;">%s</td></tr>
                          <tr><td style="padding:6px 0;color:#5f7b69;">Registered Email</td><td style="padding:6px 0;text-align:right;font-weight:600;color:#173522;">%s</td></tr>
                          <tr><td style="padding:6px 0;color:#5f7b69;">Code Expires</td><td style="padding:6px 0;text-align:right;font-weight:600;color:#173522;">%s</td></tr>
                          <tr><td style="padding:6px 0;color:#5f7b69;">Resend Available</td><td style="padding:6px 0;text-align:right;font-weight:600;color:#173522;">%s</td></tr>
                        </table>
                      </div>
                      <div style="padding:16px 18px;border-radius:16px;background:#fff8ea;border:1px solid #f1ddb1;color:#6b5112;font-size:13px;line-height:1.7;">
                        If you did not request this password change, you can safely ignore this email. Your password will remain unchanged unless the correct verification code is entered before it expires.
                      </div>
                    </div>
                    <div style="padding:18px 32px;background:#f6fbf7;border-top:1px solid #e1efe5;font-size:12px;line-height:1.7;color:#6c8375;">
                      This is an automated message from LU Librisync. Please do not reply to this email.
                    </div>
                  </div>
                </div>
                """.formatted(
                escapeHtml(student.getUser().getName()),
                escapeHtml(otpCode),
                escapeHtml(student.getStudentId()),
                escapeHtml(student.getUser().getEmail()),
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
