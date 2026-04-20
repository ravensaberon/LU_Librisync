package com.lulibrisync.controller;

import com.lulibrisync.dto.StudentProfileOtpState;
import com.lulibrisync.dto.StudentProfileUpdateRequest;
import com.lulibrisync.model.IssueRecord;
import com.lulibrisync.model.IssueStatus;
import com.lulibrisync.model.Student;
import com.lulibrisync.service.IssueService;
import com.lulibrisync.service.StudentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class StudentController {

    private static final String PROFILE_OTP_SESSION_KEY = "studentProfileOtpState";
    private static final int PROFILE_OTP_EXPIRY_MINUTES = 5;

    private final StudentService studentService;
    private final IssueService issueService;
    private final SecureRandom secureRandom = new SecureRandom();

    public StudentController(StudentService studentService, IssueService issueService) {
        this.studentService = studentService;
        this.issueService = issueService;
    }

    @GetMapping("/student/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Student student = studentService.getStudentByEmail(authentication.getName());
        List<IssueRecord> issueRecords = issueService.getStudentIssues(authentication.getName());

        long activeCount = issueRecords.stream()
                .filter(record -> !record.isReturned())
                .count();
        long overdueCount = issueRecords.stream()
                .filter(record -> IssueStatus.OVERDUE.equals(record.getStatus()))
                .count();

        model.addAttribute("student", student);
        model.addAttribute("issueRecords", issueRecords);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("overdueCount", overdueCount);
        model.addAttribute("historyCount", issueRecords.size());
        return "student/dashboard";
    }

    @GetMapping("/student/profile")
    public String profile(Authentication authentication, Model model, HttpSession session) {
        Student student = studentService.getStudentByEmail(authentication.getName());
        populateProfilePageModel(model, student, session);
        return "student/profile";
    }

    @PostMapping("/student/profile/request-otp")
    public String requestProfileUpdateOtp(Authentication authentication,
                                          @RequestParam String name,
                                          @RequestParam(required = false) String course,
                                          @RequestParam(required = false) String yearLevel,
                                          @RequestParam(required = false) String phone,
                                          @RequestParam(required = false) String address,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
                                          RedirectAttributes redirectAttributes,
                                          HttpSession session) {
        Student student = studentService.getStudentByEmail(authentication.getName());
        StudentProfileUpdateRequest submittedRequest = new StudentProfileUpdateRequest(name, course, yearLevel, phone, address, dateOfBirth);

        try {
            StudentProfileUpdateRequest normalizedRequest = studentService.normalizeProfileUpdateRequest(submittedRequest);
            String otpCode = generateOtpCode();
            StudentProfileOtpState otpState = new StudentProfileOtpState(
                    normalizedRequest,
                    otpCode,
                    LocalDateTime.now().plusMinutes(PROFILE_OTP_EXPIRY_MINUTES),
                    student.getUser().getEmail()
            );

            session.setAttribute(PROFILE_OTP_SESSION_KEY, otpState);
            redirectAttributes.addFlashAttribute("profileForm", normalizedRequest);
            redirectAttributes.addFlashAttribute("otpMaskedEmail", maskEmail(student.getUser().getEmail()));
            redirectAttributes.addFlashAttribute("otpPreviewCode", otpCode);
            redirectAttributes.addFlashAttribute("openOtpModal", true);
            redirectAttributes.addFlashAttribute("info", "Enter the one-time code to confirm your profile update.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("profileForm", submittedRequest);
            redirectAttributes.addFlashAttribute("openEditModal", true);
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/student/profile";
    }

    @PostMapping("/student/profile/verify-otp")
    public String verifyProfileUpdateOtp(Authentication authentication,
                                         @RequestParam(required = false) String otpCode,
                                         RedirectAttributes redirectAttributes,
                                         HttpSession session) {
        StudentProfileOtpState otpState = getProfileOtpState(session);
        if (otpState == null) {
            redirectAttributes.addFlashAttribute("error", "Request a profile update OTP first.");
            redirectAttributes.addFlashAttribute("openEditModal", true);
            return "redirect:/student/profile";
        }

        redirectAttributes.addFlashAttribute("profileForm", otpState.getUpdateRequest());
        redirectAttributes.addFlashAttribute("otpMaskedEmail", maskEmail(otpState.getDestinationEmail()));

        if (otpState.getExpiresAt() == null || otpState.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.removeAttribute(PROFILE_OTP_SESSION_KEY);
            redirectAttributes.addFlashAttribute("openEditModal", true);
            redirectAttributes.addFlashAttribute("error", "The OTP has expired. Please request a new code.");
            return "redirect:/student/profile";
        }

        if (otpCode == null || !otpCode.trim().equals(otpState.getOtpCode())) {
            redirectAttributes.addFlashAttribute("openOtpModal", true);
            redirectAttributes.addFlashAttribute("otpPreviewCode", otpState.getOtpCode());
            redirectAttributes.addFlashAttribute("error", "Invalid OTP. Please try again.");
            return "redirect:/student/profile";
        }

        try {
            studentService.updateProfile(authentication.getName(), otpState.getUpdateRequest());
            session.removeAttribute(PROFILE_OTP_SESSION_KEY);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully.");
        } catch (IllegalArgumentException exception) {
            session.removeAttribute(PROFILE_OTP_SESSION_KEY);
            redirectAttributes.addFlashAttribute("openEditModal", true);
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/student/profile";
    }

    @GetMapping("/student/history")
    public String history(Authentication authentication, Model model) {
        model.addAttribute("issueRecords", issueService.getStudentIssues(authentication.getName()));
        return "student/history";
    }

    private void populateProfilePageModel(Model model, Student student, HttpSession session) {
        StudentProfileOtpState otpState = getProfileOtpState(session);

        model.addAttribute("student", student);
        model.addAttribute("studentInitials", buildInitials(student.getUser().getName()));
        model.addAttribute("hasPendingProfileOtp", otpState != null);

        if (!model.containsAttribute("profileForm")) {
            model.addAttribute("profileForm", otpState != null
                    ? otpState.getUpdateRequest()
                    : studentService.createProfileUpdateRequest(student));
        }

        if (!model.containsAttribute("otpMaskedEmail") && otpState != null) {
            model.addAttribute("otpMaskedEmail", maskEmail(otpState.getDestinationEmail()));
        }
        if (!model.containsAttribute("openEditModal")) {
            model.addAttribute("openEditModal", false);
        }
        if (!model.containsAttribute("openOtpModal")) {
            model.addAttribute("openOtpModal", false);
        }
    }

    private StudentProfileOtpState getProfileOtpState(HttpSession session) {
        Object storedState = session.getAttribute(PROFILE_OTP_SESSION_KEY);
        if (storedState instanceof StudentProfileOtpState otpState) {
            return otpState;
        }
        return null;
    }

    private String generateOtpCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    private String buildInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "ST";
        }

        String[] parts = fullName.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            if (!part.isBlank()) {
                initials.append(Character.toUpperCase(part.charAt(0)));
            }
            if (initials.length() == 2) {
                break;
            }
        }
        return initials.isEmpty() ? "ST" : initials.toString();
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
}
