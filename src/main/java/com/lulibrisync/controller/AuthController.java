package com.lulibrisync.controller;

import com.lulibrisync.dto.PasswordResetOtpDispatchResult;
import com.lulibrisync.dto.PasswordResetOtpState;
import com.lulibrisync.dto.RegistrationAvailabilityResult;
import com.lulibrisync.dto.StudentRegistrationForm;
import com.lulibrisync.dto.StudentRegistrationOtpDispatchResult;
import com.lulibrisync.dto.StudentRegistrationOtpState;
import com.lulibrisync.service.AuthService;
import com.lulibrisync.service.PasswordResetService;
import com.lulibrisync.service.StudentRegistrationOtpService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Controller
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final StudentRegistrationOtpService studentRegistrationOtpService;

    public AuthController(AuthService authService,
                          PasswordResetService passwordResetService,
                          StudentRegistrationOtpService studentRegistrationOtpService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
        this.studentRegistrationOtpService = studentRegistrationOtpService;
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(@RequestParam(required = false) String email,
                               Authentication authentication,
                               Model model) {
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }
        populateRegisterPageModel(model, email);
        return "auth/register";
    }

    @GetMapping("/register/otp")
    public String registerOtpPage(@RequestParam(required = false) String email,
                                  Authentication authentication,
                                  Model model) {
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }
        if (!populateRegisterOtpPageModel(model, email)) {
            return "redirect:/register";
        }
        return "auth/register-otp";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(@RequestParam(required = false) String email,
                                     Authentication authentication,
                                     Model model) {
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }
        populateForgotPasswordModel(model, email);
        return "auth/forgot-password";
    }

    @GetMapping("/register/barangays")
    @ResponseBody
    public ResponseEntity<List<String>> loadBarangays(@RequestParam(required = false) String cityMunicipality) {
        try {
            return ResponseEntity.ok(authService.getBarangaysForCityMunicipality(cityMunicipality));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(503).build();
        }
    }

    @GetMapping("/register/availability")
    @ResponseBody
    public ResponseEntity<RegistrationAvailabilityResult> checkRegistrationAvailability(@RequestParam(required = false) String field,
                                                                                        @RequestParam(required = false) String value) {
        RegistrationAvailabilityResult result = authService.checkRegistrationAvailability(field, value);
        if (!result.valid() && "Unsupported registration field.".equals(result.message())) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/register")
    public String register(@ModelAttribute StudentRegistrationForm registrationForm,
                           Authentication authentication,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }
        try {
            StudentRegistrationOtpDispatchResult dispatchResult = studentRegistrationOtpService.requestOtp(registrationForm);
            applyRegistrationOtpStateFlashAttributes(dispatchResult.getOtpState(), redirectAttributes);
            if (dispatchResult.isCooldownActive()) {
                redirectAttributes.addFlashAttribute("info", "A registration OTP is already active.");
            } else if (dispatchResult.isDelivered()) {
                redirectAttributes.addFlashAttribute("success", "A registration OTP has been sent to your email.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Unable to send OTP email right now.");
            }
            return registrationOtpRedirect(dispatchResult.getOtpState().getEmail());
        } catch (IllegalArgumentException exception) {
            populateRegisterModel(model, registrationForm);
            if (!applyRegisterFieldError(model, exception.getMessage())) {
                model.addAttribute("error", exception.getMessage());
            }
            return "auth/register";
        }
    }

    @PostMapping("/register/resend-otp")
    public String resendRegistrationOtp(@RequestParam(required = false) String email,
                                        RedirectAttributes redirectAttributes) {
        try {
            StudentRegistrationOtpDispatchResult dispatchResult = studentRegistrationOtpService.resendOtp(email);
            applyRegistrationOtpStateFlashAttributes(dispatchResult.getOtpState(), redirectAttributes);
            if (dispatchResult.isCooldownActive()) {
                redirectAttributes.addFlashAttribute("info", "Please wait before requesting another OTP.");
            } else if (dispatchResult.isDelivered()) {
                redirectAttributes.addFlashAttribute("success", "A fresh registration OTP has been sent to your email.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Unable to send OTP email right now.");
            }
            return registrationOtpRedirect(dispatchResult.getOtpState().getEmail());
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("emailValue", email);
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return registrationOtpRedirect(email);
        }
    }

    @PostMapping("/register/verify-otp")
    public String verifyRegistrationOtp(@RequestParam(required = false) String email,
                                        @RequestParam(required = false) String otpCode,
                                        RedirectAttributes redirectAttributes) {
        try {
            studentRegistrationOtpService.verifyOtp(email, otpCode);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("emailValue", email);
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            StudentRegistrationOtpState otpState = studentRegistrationOtpService.getActiveOtpState(email);
            if (otpState != null) {
                applyRegistrationOtpStateFlashAttributes(otpState, redirectAttributes);
            }
            return registrationOtpRedirect(email);
        }
    }

    @PostMapping("/forgot-password/request-otp")
    public String requestPasswordResetOtp(@RequestParam(required = false) String email,
                                          RedirectAttributes redirectAttributes) {
        try {
            PasswordResetOtpDispatchResult dispatchResult = passwordResetService.requestOtp(email);
            applyPasswordResetStateFlashAttributes(dispatchResult.getOtpState(), redirectAttributes);
            redirectAttributes.addFlashAttribute("openResetPanel", true);
            if (dispatchResult.isCooldownActive()) {
                redirectAttributes.addFlashAttribute("info", "A password reset OTP is already active.");
            } else if (dispatchResult.isDelivered()) {
                redirectAttributes.addFlashAttribute("success", "A password reset OTP has been sent to your email.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Unable to send OTP email right now.");
            }
            return "redirect:/forgot-password?email=" + dispatchResult.getOtpState().getEmail();
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("emailValue", email);
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/forgot-password";
        }
    }

    @PostMapping("/forgot-password/resend-otp")
    public String resendPasswordResetOtp(@RequestParam(required = false) String email,
                                         RedirectAttributes redirectAttributes) {
        try {
            PasswordResetOtpDispatchResult dispatchResult = passwordResetService.resendOtp(email);
            applyPasswordResetStateFlashAttributes(dispatchResult.getOtpState(), redirectAttributes);
            redirectAttributes.addFlashAttribute("openResetPanel", true);
            if (dispatchResult.isCooldownActive()) {
                redirectAttributes.addFlashAttribute("info", "Please wait before requesting another OTP.");
            } else if (dispatchResult.isDelivered()) {
                redirectAttributes.addFlashAttribute("success", "A fresh password reset OTP has been sent to your email.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Unable to send OTP email right now.");
            }
            return "redirect:/forgot-password?email=" + dispatchResult.getOtpState().getEmail();
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("emailValue", email);
            redirectAttributes.addFlashAttribute("openResetPanel", true);
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/forgot-password";
        }
    }

    @PostMapping("/forgot-password/reset")
    public String resetPassword(@RequestParam(required = false) String email,
                                @RequestParam(required = false) String otpCode,
                                @RequestParam(required = false) String newPassword,
                                @RequestParam(required = false) String confirmPassword,
                                RedirectAttributes redirectAttributes) {
        try {
            passwordResetService.resetPassword(email, otpCode, newPassword, confirmPassword);
            return "redirect:/login?resetSuccess";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("emailValue", email);
            redirectAttributes.addFlashAttribute("openResetPanel", true);
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            try {
                PasswordResetOtpState otpState = passwordResetService.getActiveOtpState(email);
                applyPasswordResetStateFlashAttributes(otpState, redirectAttributes);
            } catch (IllegalArgumentException ignored) {
                // Keep the entered email visible even if no active OTP exists.
            }
            return "redirect:/forgot-password";
        }
    }

    private void populateRegisterModel(Model model, StudentRegistrationForm registrationForm) {
        if (registrationForm == null) {
            populateRegisterOptions(model);
            return;
        }
        populateRegisterModel(
                model,
                registrationForm.getFirstName(),
                registrationForm.getMiddleName(),
                registrationForm.getLastName(),
                registrationForm.getProgram(),
                registrationForm.getYearLevel(),
                registrationForm.getEmail(),
                registrationForm.getContactNumber(),
                registrationForm.getBirthDate(),
                registrationForm.getProvince(),
                registrationForm.getCityMunicipality(),
                registrationForm.getBarangay(),
                registrationForm.getStreet(),
                registrationForm.getZipcode(),
                registrationForm.isAgreeChecked()
        );
    }

    private void populateRegisterModel(Model model,
                                       String firstName,
                                       String middleName,
                                       String lastName,
                                       String program,
                                       String yearLevel,
                                       String email,
                                       String contactNumber,
                                       String birthDate,
                                       String province,
                                       String cityMunicipality,
                                       String barangay,
                                       String street,
                                       String zipcode,
                                       boolean agreeChecked) {
        populateRegisterOptions(model);
        model.addAttribute("firstNameValue", firstName);
        model.addAttribute("middleNameValue", middleName);
        model.addAttribute("lastNameValue", lastName);
        model.addAttribute("programValue", program);
        model.addAttribute("yearLevelValue", yearLevel);
        model.addAttribute("emailValue", email);
        model.addAttribute("contactNumberValue", contactNumber);
        model.addAttribute("birthDateValue", birthDate);
        model.addAttribute("provinceValue", province);
        model.addAttribute("cityMunicipalityValue", cityMunicipality);
        model.addAttribute("barangayValue", barangay);
        model.addAttribute("streetValue", street);
        model.addAttribute("zipcodeValue", zipcode);
        model.addAttribute("agreeChecked", agreeChecked);
    }

    private void populateRegisterPageModel(Model model, String email) {
        populateRegisterOptions(model);
        String effectiveEmail = resolveEmailForRegister(model, email);
        if (!model.containsAttribute("emailValue")) {
            model.addAttribute("emailValue", effectiveEmail);
        }

        if (effectiveEmail == null || effectiveEmail.isBlank()) {
            return;
        }

        StudentRegistrationOtpState latestState = studentRegistrationOtpService.getLatestOtpState(effectiveEmail);
        if (latestState != null && !model.containsAttribute("firstNameValue")) {
            populateRegisterModel(model, latestState.getRegistrationForm());
        }
    }

    private boolean populateRegisterOtpPageModel(Model model, String email) {
        String effectiveEmail = resolveEmailForRegister(model, email);
        if (!model.containsAttribute("emailValue")) {
            model.addAttribute("emailValue", effectiveEmail);
        }

        if (effectiveEmail == null || effectiveEmail.isBlank()) {
            return false;
        }

        StudentRegistrationOtpState latestState = studentRegistrationOtpService.getLatestOtpState(effectiveEmail);
        if (latestState == null) {
            return false;
        }

        StudentRegistrationOtpState activeState = studentRegistrationOtpService.getActiveOtpState(effectiveEmail);
        StudentRegistrationOtpState displayState = activeState != null ? activeState : latestState;

        if (!model.containsAttribute("registrationOtpMaskedEmail")) {
            model.addAttribute("registrationOtpMaskedEmail", displayState.getMaskedEmail());
        }
        if (!model.containsAttribute("hasPendingRegistrationOtp")) {
            model.addAttribute("hasPendingRegistrationOtp", activeState != null);
        }
        if (!model.containsAttribute("registrationOtpExpired")) {
            model.addAttribute("registrationOtpExpired", activeState == null);
        }
        if (!model.containsAttribute("registrationOtpExpiresAtEpochMs")) {
            model.addAttribute("registrationOtpExpiresAtEpochMs", toEpochMillis(displayState.getExpiresAt()));
        }
        if (!model.containsAttribute("registrationOtpResendAvailableAtEpochMs")) {
            model.addAttribute("registrationOtpResendAvailableAtEpochMs", toEpochMillis(displayState.getResendAvailableAt()));
        }
        return true;
    }

    private void populateRegisterOptions(Model model) {
        model.addAttribute("registrationCityZipCodes", authService.getLagunaCityZipCodes());
        if (!model.containsAttribute("provinceValue")) {
            model.addAttribute("provinceValue", "Laguna");
        }
    }

    private boolean applyRegisterFieldError(Model model, String message) {
        if (message == null || message.isBlank()) {
            return false;
        }
        if ("This email is already taken.".equals(message)) {
            model.addAttribute("emailFieldError", message);
            return true;
        }
        if ("This contact number is already used.".equals(message)) {
            model.addAttribute("contactNumberFieldError", message);
            return true;
        }
        return false;
    }

    private String resolveEmailForRegister(Model model, String email) {
        if (model.containsAttribute("emailValue")) {
            Object existingValue = model.getAttribute("emailValue");
            return existingValue == null ? null : existingValue.toString();
        }
        return email;
    }

    private void applyRegistrationOtpStateFlashAttributes(StudentRegistrationOtpState otpState,
                                                          RedirectAttributes redirectAttributes) {
        if (otpState == null) {
            return;
        }
        redirectAttributes.addFlashAttribute("emailValue", otpState.getEmail());
        redirectAttributes.addFlashAttribute("registrationOtpMaskedEmail", otpState.getMaskedEmail());
        redirectAttributes.addFlashAttribute("hasPendingRegistrationOtp", true);
        redirectAttributes.addFlashAttribute("registrationOtpExpired", false);
        redirectAttributes.addFlashAttribute("registrationOtpExpiresAtEpochMs", toEpochMillis(otpState.getExpiresAt()));
        redirectAttributes.addFlashAttribute("registrationOtpResendAvailableAtEpochMs", toEpochMillis(otpState.getResendAvailableAt()));
    }

    private void populateForgotPasswordModel(Model model, String email) {
        String effectiveEmail = resolveEmailForForgotPassword(model, email);
        if (!model.containsAttribute("openResetPanel")) {
            model.addAttribute("openResetPanel", false);
        }

        if (!model.containsAttribute("emailValue")) {
            model.addAttribute("emailValue", effectiveEmail);
        }

        if (effectiveEmail == null || effectiveEmail.isBlank()) {
            if (!model.containsAttribute("hasPendingResetOtp")) {
                model.addAttribute("hasPendingResetOtp", false);
            }
            return;
        }

        try {
            PasswordResetOtpState activeState = passwordResetService.getActiveOtpState(effectiveEmail);
            boolean hasActiveOtp = activeState != null;
            model.addAttribute("hasPendingResetOtp", hasActiveOtp);
            if (hasActiveOtp) {
                if (!model.containsAttribute("maskedResetEmail")) {
                    model.addAttribute("maskedResetEmail", activeState.getMaskedEmail());
                }
                if (!model.containsAttribute("resetOtpExpiresAtEpochMs")) {
                    model.addAttribute("resetOtpExpiresAtEpochMs", toEpochMillis(activeState.getExpiresAt()));
                }
                if (!model.containsAttribute("resetOtpResendAvailableAtEpochMs")) {
                    model.addAttribute("resetOtpResendAvailableAtEpochMs", toEpochMillis(activeState.getResendAvailableAt()));
                }
                if (!(Boolean.TRUE.equals(model.getAttribute("openResetPanel")))) {
                    model.addAttribute("openResetPanel", true);
                }
            }
        } catch (IllegalArgumentException ignored) {
            if (!model.containsAttribute("hasPendingResetOtp")) {
                model.addAttribute("hasPendingResetOtp", false);
            }
        }
    }

    private String resolveEmailForForgotPassword(Model model, String email) {
        if (model.containsAttribute("emailValue")) {
            Object existingValue = model.getAttribute("emailValue");
            return existingValue == null ? null : existingValue.toString();
        }
        return email;
    }

    private void applyPasswordResetStateFlashAttributes(PasswordResetOtpState otpState,
                                                        RedirectAttributes redirectAttributes) {
        if (otpState == null) {
            return;
        }
        redirectAttributes.addFlashAttribute("emailValue", otpState.getEmail());
        redirectAttributes.addFlashAttribute("maskedResetEmail", otpState.getMaskedEmail());
        redirectAttributes.addFlashAttribute("hasPendingResetOtp", true);
        redirectAttributes.addFlashAttribute("resetOtpExpiresAtEpochMs", toEpochMillis(otpState.getExpiresAt()));
        redirectAttributes.addFlashAttribute("resetOtpResendAvailableAtEpochMs", toEpochMillis(otpState.getResendAvailableAt()));
    }

    private Long toEpochMillis(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private String urlEncode(String value) {
        return value == null ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String registrationOtpRedirect(String email) {
        return "redirect:/register/otp?email=" + urlEncode(email);
    }
}
