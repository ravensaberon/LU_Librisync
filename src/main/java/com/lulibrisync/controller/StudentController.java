package com.lulibrisync.controller;

import com.lulibrisync.dto.StudentProfileOtpDispatchResult;
import com.lulibrisync.dto.StudentProfileOtpState;
import com.lulibrisync.dto.StudentProfileUpdateRequest;
import com.lulibrisync.model.IssueRecord;
import com.lulibrisync.model.IssueStatus;
import com.lulibrisync.model.Student;
import com.lulibrisync.service.AuthService;
import com.lulibrisync.service.FineService;
import com.lulibrisync.service.IssueService;
import com.lulibrisync.service.ReservationService;
import com.lulibrisync.service.StudentProfileImageService;
import com.lulibrisync.service.StudentProfileOtpService;
import com.lulibrisync.service.StudentService;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller
public class StudentController {

    private final StudentService studentService;
    private final IssueService issueService;
    private final ReservationService reservationService;
    private final StudentProfileOtpService studentProfileOtpService;
    private final FineService fineService;
    private final StudentProfileImageService studentProfileImageService;
    private final AuthService authService;

    public StudentController(StudentService studentService,
                             IssueService issueService,
                             ReservationService reservationService,
                             StudentProfileOtpService studentProfileOtpService,
                             FineService fineService,
                             StudentProfileImageService studentProfileImageService,
                             AuthService authService) {
        this.studentService = studentService;
        this.issueService = issueService;
        this.reservationService = reservationService;
        this.studentProfileOtpService = studentProfileOtpService;
        this.fineService = fineService;
        this.studentProfileImageService = studentProfileImageService;
        this.authService = authService;
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
        model.addAttribute("reservationCount", reservationService.getStudentReservations(authentication.getName()).stream().filter(reservation -> reservation.isActive()).count());
        model.addAttribute("borrowerStanding", studentService.getBorrowerStanding(student));
        model.addAttribute("outstandingFineTotal", fineService.getOutstandingFineTotalByStudent(student.getId()));
        model.addAttribute("studentFines", fineService.getStudentFines(student.getId()));
        model.addAttribute("popularBooks", issueService.getMostBorrowedBooks());
        return "student/dashboard";
    }

    @GetMapping("/student/profile")
    public String profile(Authentication authentication, Model model) {
        Student student = studentService.getStudentByEmail(authentication.getName());
        populateProfilePageModel(model, student);
        return "student/profile";
    }

    @GetMapping("/student/profile/avatar")
    public ResponseEntity<Resource> profileAvatar(Authentication authentication) {
        Student student = studentService.getStudentByEmail(authentication.getName());
        Resource resource = studentProfileImageService.getProfileImageResource(student);

        return ResponseEntity.ok()
                .contentType(studentProfileImageService.getProfileImageMediaType(student))
                .cacheControl(CacheControl.maxAge(1, TimeUnit.MINUTES).cachePrivate())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(resource);
    }

    @PostMapping("/student/profile/avatar")
    public String uploadProfileAvatar(Authentication authentication,
                                      @RequestParam("profileImage") MultipartFile profileImage,
                                      RedirectAttributes redirectAttributes) {
        Student student = studentService.getStudentByEmail(authentication.getName());
        try {
            studentProfileImageService.storeProfileImage(student, profileImage);
            redirectAttributes.addFlashAttribute("success", "Profile picture updated successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/student/profile";
    }

    @PostMapping("/student/profile/avatar/remove")
    public String removeProfileAvatar(Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        Student student = studentService.getStudentByEmail(authentication.getName());
        studentProfileImageService.deleteProfileImage(student);
        redirectAttributes.addFlashAttribute("success", "Profile picture removed.");
        return "redirect:/student/profile";
    }

    @PostMapping("/student/profile/request-otp")
    public String requestProfileUpdateOtp(Authentication authentication,
                                          @RequestParam String name,
                                          @RequestParam(required = false) String course,
                                          @RequestParam(required = false) String yearLevel,
                                          @RequestParam(required = false) String phone,
                                          @RequestParam(required = false) String province,
                                          @RequestParam(required = false) String cityMunicipality,
                                          @RequestParam(required = false) String barangay,
                                          @RequestParam(required = false) String street,
                                          @RequestParam(required = false) String zipcode,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
                                          RedirectAttributes redirectAttributes) {
        Student student = studentService.getStudentByEmail(authentication.getName());
        StudentProfileUpdateRequest submittedRequest = new StudentProfileUpdateRequest(name, course, yearLevel, phone, null, dateOfBirth);

        try {
            submittedRequest.setAddress(authService.normalizeAndBuildOptionalAddress(province, cityMunicipality, barangay, street, zipcode));
            StudentProfileOtpDispatchResult dispatchResult = studentProfileOtpService.requestOtp(student, submittedRequest);
            applyOtpStateFlashAttributes(dispatchResult.getOtpState(), redirectAttributes);
            redirectAttributes.addFlashAttribute("openOtpModal", true);
            if (dispatchResult.isCooldownActive()) {
                redirectAttributes.addFlashAttribute("info", "A profile OTP is already active.");
            } else if (dispatchResult.isDelivered()) {
                redirectAttributes.addFlashAttribute("success", "An OTP has been sent to your registered email.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Unable to send OTP email right now.");
            }
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("profileForm", submittedRequest);
            applyAddressFlashAttributes("profile", province, cityMunicipality, barangay, street, zipcode, redirectAttributes);
            redirectAttributes.addFlashAttribute("openEditModal", true);
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/student/profile";
    }

    @PostMapping("/student/profile/resend-otp")
    public String resendProfileUpdateOtp(Authentication authentication,
                                         RedirectAttributes redirectAttributes) {
        Student student = studentService.getStudentByEmail(authentication.getName());
        try {
            StudentProfileOtpDispatchResult dispatchResult = studentProfileOtpService.resendOtp(student);
            applyOtpStateFlashAttributes(dispatchResult.getOtpState(), redirectAttributes);
            redirectAttributes.addFlashAttribute("openOtpModal", true);
            if (dispatchResult.isCooldownActive()) {
                redirectAttributes.addFlashAttribute("info", "Please wait before requesting another OTP.");
            } else if (dispatchResult.isDelivered()) {
                redirectAttributes.addFlashAttribute("success", "A new OTP has been sent to your registered email.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Unable to send OTP email right now.");
            }
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("openEditModal", true);
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/student/profile";
    }

    @PostMapping("/student/profile/verify-otp")
    public String verifyProfileUpdateOtp(Authentication authentication,
                                         @RequestParam(required = false) String otpCode,
                                         RedirectAttributes redirectAttributes) {
        Student student = studentService.getStudentByEmail(authentication.getName());
        StudentProfileOtpState latestOtpState = studentProfileOtpService.getLatestOtpState(student);
        if (latestOtpState != null) {
            applyOtpStateFlashAttributes(latestOtpState, redirectAttributes);
            redirectAttributes.addFlashAttribute("profileForm", latestOtpState.getUpdateRequest());
        }

        try {
            StudentProfileUpdateRequest verifiedRequest = studentProfileOtpService.verifyOtp(student, otpCode);
            studentService.updateProfile(authentication.getName(), verifiedRequest);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully.");
        } catch (IllegalArgumentException exception) {
            boolean hasActiveOtp = latestOtpState != null
                    && latestOtpState.getExpiresAt() != null
                    && latestOtpState.getExpiresAt().isAfter(LocalDateTime.now());
            redirectAttributes.addFlashAttribute("openOtpModal", hasActiveOtp);
            redirectAttributes.addFlashAttribute("openEditModal", !hasActiveOtp);
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/student/profile";
    }

    @PostMapping("/student/profile/password")
    public String changePassword(Authentication authentication,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {
        try {
            studentService.changePassword(authentication.getName(), currentPassword, newPassword, confirmPassword);
            redirectAttributes.addFlashAttribute("success", "Password changed successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/student/profile#password-security";
    }

    @GetMapping("/student/history")
    public String history(Authentication authentication, Model model) {
        Student student = studentService.getStudentByEmail(authentication.getName());
        List<IssueRecord> issueRecords = issueService.getStudentIssues(authentication.getName());
        model.addAttribute("student", student);
        model.addAttribute("issueRecords", issueRecords);
        model.addAttribute("borrowerStanding", studentService.getBorrowerStanding(student));
        model.addAttribute("outstandingFineTotal", fineService.getOutstandingFineTotalByStudent(student.getId()));
        model.addAttribute("activeCount", issueRecords.stream().filter(record -> !record.isReturned()).count());
        model.addAttribute("overdueCount", issueRecords.stream().filter(record -> IssueStatus.OVERDUE.equals(record.getStatus())).count());
        model.addAttribute("reservationCount", reservationService.getStudentReservations(authentication.getName()).stream().filter(reservation -> reservation.isActive()).count());
        return "student/history";
    }

    private void populateProfilePageModel(Model model, Student student) {
        StudentProfileOtpState activeOtpState = studentProfileOtpService.getActiveOtpState(student);
        StudentProfileOtpState latestOtpState = studentProfileOtpService.getLatestOtpState(student);

        model.addAttribute("student", student);
        model.addAttribute("studentInitials", buildInitials(student.getUser().getName()));
        model.addAttribute("hasProfileImage", studentProfileImageService.hasProfileImage(student));
        model.addAttribute("profileImageVersion", studentProfileImageService.getProfileImageVersion(student));
        model.addAttribute("hasPendingProfileOtp", activeOtpState != null);
        model.addAttribute("borrowerStanding", studentService.getBorrowerStanding(student));
        model.addAttribute("studentFines", fineService.getStudentFines(student.getId()));
        model.addAttribute("outstandingFineTotal", fineService.getOutstandingFineTotalByStudent(student.getId()));

        if (!model.containsAttribute("profileForm")) {
            model.addAttribute("profileForm", latestOtpState != null
                    ? latestOtpState.getUpdateRequest()
                    : studentService.createProfileUpdateRequest(student));
        }

        StudentProfileUpdateRequest profileForm = (StudentProfileUpdateRequest) model.getAttribute("profileForm");
        populateAddressModelAttributes("profile", profileForm == null ? null : profileForm.getAddress(), model);

        if (!model.containsAttribute("otpMaskedEmail") && activeOtpState != null) {
            model.addAttribute("otpMaskedEmail", maskEmail(activeOtpState.getDestinationEmail()));
        }
        if (!model.containsAttribute("otpExpiresAtEpochMs")) {
            model.addAttribute("otpExpiresAtEpochMs", toEpochMillis(activeOtpState == null ? null : activeOtpState.getExpiresAt()));
        }
        if (!model.containsAttribute("otpResendAvailableAtEpochMs")) {
            model.addAttribute("otpResendAvailableAtEpochMs", toEpochMillis(activeOtpState == null ? null : activeOtpState.getResendAvailableAt()));
        }
        if (!model.containsAttribute("openEditModal")) {
            model.addAttribute("openEditModal", false);
        }
        if (!model.containsAttribute("openOtpModal")) {
            model.addAttribute("openOtpModal", false);
        }
    }

    private void applyOtpStateFlashAttributes(StudentProfileOtpState otpState,
                                              RedirectAttributes redirectAttributes) {
        if (otpState == null) {
            return;
        }
        redirectAttributes.addFlashAttribute("profileForm", otpState.getUpdateRequest());
        applyAddressFlashAttributesFromAddress("profile", otpState.getUpdateRequest().getAddress(), redirectAttributes);
        redirectAttributes.addFlashAttribute("otpMaskedEmail", maskEmail(otpState.getDestinationEmail()));
        redirectAttributes.addFlashAttribute("otpExpiresAtEpochMs", toEpochMillis(otpState.getExpiresAt()));
        redirectAttributes.addFlashAttribute("otpResendAvailableAtEpochMs", toEpochMillis(otpState.getResendAvailableAt()));
    }

    private void populateAddressModelAttributes(String prefix, String address, Model model) {
        com.lulibrisync.util.AddressFormValue addressFormValue = authService.parseAddress(address);
        if (!model.containsAttribute(prefix + "ProvinceValue")) {
            model.addAttribute(prefix + "ProvinceValue", addressFormValue.getProvince());
        }
        if (!model.containsAttribute(prefix + "CityMunicipalityValue")) {
            model.addAttribute(prefix + "CityMunicipalityValue", addressFormValue.getCityMunicipality());
        }
        if (!model.containsAttribute(prefix + "BarangayValue")) {
            model.addAttribute(prefix + "BarangayValue", addressFormValue.getBarangay());
        }
        if (!model.containsAttribute(prefix + "StreetValue")) {
            model.addAttribute(prefix + "StreetValue", addressFormValue.getStreet());
        }
        if (!model.containsAttribute(prefix + "ZipcodeValue")) {
            model.addAttribute(prefix + "ZipcodeValue", addressFormValue.getZipcode());
        }
    }

    private void applyAddressFlashAttributes(String prefix,
                                             String province,
                                             String cityMunicipality,
                                             String barangay,
                                             String street,
                                             String zipcode,
                                             RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(prefix + "ProvinceValue", province);
        redirectAttributes.addFlashAttribute(prefix + "CityMunicipalityValue", cityMunicipality);
        redirectAttributes.addFlashAttribute(prefix + "BarangayValue", barangay);
        redirectAttributes.addFlashAttribute(prefix + "StreetValue", street);
        redirectAttributes.addFlashAttribute(prefix + "ZipcodeValue", zipcode);
    }

    private void applyAddressFlashAttributesFromAddress(String prefix,
                                                        String address,
                                                        RedirectAttributes redirectAttributes) {
        com.lulibrisync.util.AddressFormValue addressFormValue = authService.parseAddress(address);
        applyAddressFlashAttributes(
                prefix,
                addressFormValue.getProvince(),
                addressFormValue.getCityMunicipality(),
                addressFormValue.getBarangay(),
                addressFormValue.getStreet(),
                addressFormValue.getZipcode(),
                redirectAttributes
        );
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

    private Long toEpochMillis(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
