package com.lulibrisync.controller;

import com.lulibrisync.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }
        populateRegisterOptions(model);
        return "auth/register";
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

    @PostMapping("/register")
    public String register(@RequestParam(required = false) String firstName,
                           @RequestParam(required = false) String middleName,
                           @RequestParam(required = false) String lastName,
                           @RequestParam(required = false) String program,
                           @RequestParam(required = false) String email,
                           @RequestParam(required = false) String contactNumber,
                           @RequestParam(required = false) String birthDate,
                           @RequestParam(required = false) String province,
                           @RequestParam(required = false) String cityMunicipality,
                           @RequestParam(required = false) String barangay,
                           @RequestParam(required = false) String street,
                           @RequestParam(required = false) String zipcode,
                           @RequestParam(required = false) String password,
                           @RequestParam(required = false) String confirmPassword,
                           @RequestParam(required = false) String agree,
                           Model model) {
        try {
            authService.registerStudent(
                    firstName,
                    middleName,
                    lastName,
                    program,
                    email,
                    contactNumber,
                    birthDate,
                    province,
                    cityMunicipality,
                    barangay,
                    street,
                    zipcode,
                    password,
                    confirmPassword,
                    agree != null
            );
            return "redirect:/login?registered";
        } catch (IllegalArgumentException exception) {
            populateRegisterModel(
                    model,
                    firstName,
                    middleName,
                    lastName,
                    program,
                    email,
                    contactNumber,
                    birthDate,
                    province,
                    cityMunicipality,
                    barangay,
                    street,
                    zipcode,
                    agree != null
            );
            model.addAttribute("error", exception.getMessage());
            return "auth/register";
        }
    }

    private void populateRegisterModel(Model model,
                                       String firstName,
                                       String middleName,
                                       String lastName,
                                       String program,
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

    private void populateRegisterOptions(Model model) {
        model.addAttribute("registrationCityZipCodes", authService.getLagunaCityZipCodes());
        if (!model.containsAttribute("provinceValue")) {
            model.addAttribute("provinceValue", "Laguna");
        }
    }
}
