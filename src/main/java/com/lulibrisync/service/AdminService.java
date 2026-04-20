package com.lulibrisync.service;

import com.lulibrisync.config.LegacyAwarePasswordEncoder;
import com.lulibrisync.model.Role;
import com.lulibrisync.model.User;
import com.lulibrisync.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final LegacyAwarePasswordEncoder passwordEncoder;

    public AdminService(UserRepository userRepository,
                        LegacyAwarePasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getAdminByEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(required(email, "Admin account not found."))
                .orElseThrow(() -> new IllegalArgumentException("Admin account not found."));

        if (!Role.ADMIN.equals(user.getRole())) {
            throw new IllegalArgumentException("Selected user is not an admin account.");
        }

        return user;
    }

    @Transactional
    public User updateProfile(String email, String name) {
        User admin = getAdminByEmail(email);
        admin.setName(required(name, "Display name is required."));
        return userRepository.save(admin);
    }

    @Transactional
    public void changePassword(String email,
                               String currentPassword,
                               String newPassword,
                               String confirmPassword) {
        User admin = getAdminByEmail(email);
        String normalizedCurrentPassword = required(currentPassword, "Current password is required.");
        String normalizedNewPassword = required(newPassword, "New password is required.");
        String normalizedConfirmPassword = required(confirmPassword, "Please confirm the new password.");

        if (!passwordEncoder.matches(normalizedCurrentPassword, admin.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        if (normalizedNewPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters.");
        }
        if (!normalizedNewPassword.equals(normalizedConfirmPassword)) {
            throw new IllegalArgumentException("New password and confirmation do not match.");
        }
        if (passwordEncoder.matches(normalizedNewPassword, admin.getPasswordHash())) {
            throw new IllegalArgumentException("Choose a different password from the current one.");
        }

        admin.setPasswordHash(passwordEncoder.encode(normalizedNewPassword));
        userRepository.save(admin);
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
