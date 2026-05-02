package com.lulibrisync.service;

import com.lulibrisync.model.AdminNotification;
import com.lulibrisync.model.AdminNotificationType;
import com.lulibrisync.model.Role;
import com.lulibrisync.model.User;
import com.lulibrisync.repository.AdminNotificationRepository;
import com.lulibrisync.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminNotificationService {

    private final AdminNotificationRepository adminNotificationRepository;
    private final UserRepository userRepository;
    private final AdminService adminService;

    public AdminNotificationService(AdminNotificationRepository adminNotificationRepository,
                                    UserRepository userRepository,
                                    AdminService adminService) {
        this.adminNotificationRepository = adminNotificationRepository;
        this.userRepository = userRepository;
        this.adminService = adminService;
    }

    @Transactional
    public void notifyAdmins(AdminNotificationType notificationType,
                             String title,
                             String message,
                             String linkUrl) {
        List<User> admins = userRepository.findAllByRole(Role.ADMIN);
        for (User admin : admins) {
            AdminNotification notification = new AdminNotification();
            notification.setAdminUser(admin);
            notification.setNotificationType(notificationType);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setLinkUrl(linkUrl);
            notification.setRead(false);
            notification.setReadAt(null);
            adminNotificationRepository.save(notification);
        }
    }

    public List<AdminNotification> getRecentNotifications(String adminEmail) {
        adminService.getAdminByEmail(adminEmail);
        return adminNotificationRepository.findTop10ByAdminUser_EmailIgnoreCaseOrderByCreatedAtDesc(adminEmail);
    }

    public long countUnreadNotifications(String adminEmail) {
        adminService.getAdminByEmail(adminEmail);
        return adminNotificationRepository.countByAdminUser_EmailIgnoreCaseAndReadFalse(adminEmail);
    }

    @Transactional
    public void markAllAsRead(String adminEmail) {
        adminService.getAdminByEmail(adminEmail);
        List<AdminNotification> unreadNotifications = adminNotificationRepository
                .findByAdminUser_EmailIgnoreCaseAndReadFalseOrderByCreatedAtDesc(adminEmail);
        for (AdminNotification notification : unreadNotifications) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        }
        adminNotificationRepository.saveAll(unreadNotifications);
    }
}
