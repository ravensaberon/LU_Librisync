package com.lulibrisync.controller;

import com.lulibrisync.model.IssueRecord;
import com.lulibrisync.model.IssueStatus;
import com.lulibrisync.model.Role;
import com.lulibrisync.model.Student;
import com.lulibrisync.model.User;
import com.lulibrisync.model.UserStatus;
import com.lulibrisync.repository.BookRepository;
import com.lulibrisync.repository.IssueRecordRepository;
import com.lulibrisync.repository.UserRepository;
import com.lulibrisync.service.AdminService;
import com.lulibrisync.service.AuthService;
import com.lulibrisync.service.IssueService;
import com.lulibrisync.service.StudentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final IssueRecordRepository issueRecordRepository;
    private final IssueService issueService;
    private final StudentService studentService;
    private final AdminService adminService;
    private final AuthService authService;

    public AdminController(BookRepository bookRepository,
                           UserRepository userRepository,
                           IssueRecordRepository issueRecordRepository,
                           IssueService issueService,
                           StudentService studentService,
                           AdminService adminService,
                           AuthService authService) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.issueRecordRepository = issueRecordRepository;
        this.issueService = issueService;
        this.studentService = studentService;
        this.adminService = adminService;
        this.authService = authService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        issueService.refreshOverdueStatuses();
        long issuedCount = issueRecordRepository.countByStatus(IssueStatus.ISSUED);
        long overdueCount = issueRecordRepository.countByStatus(IssueStatus.OVERDUE);

        model.addAttribute("bookCount", bookRepository.count());
        model.addAttribute("availableCount", bookRepository.countByAvailableQuantityGreaterThan(0));
        model.addAttribute("studentCount", userRepository.countByRole(Role.STUDENT));
        model.addAttribute("issuedCount", issuedCount);
        model.addAttribute("overdueCount", overdueCount);
        model.addAttribute("overdueRate", (issuedCount + overdueCount) == 0 ? 0 : (overdueCount * 100) / (issuedCount + overdueCount));
        model.addAttribute("recentIssues", issueService.getRecentIssues());
        model.addAttribute("mostBorrowedBooks", issueService.getMostBorrowedBooks());
        model.addAttribute("weeklyChart", issueService.getWeeklyCirculationChart());
        return "admin/dashboard";
    }

    @GetMapping("/students")
    public String students(@RequestParam(required = false) String studentId,
                           @RequestParam(required = false) String modalStudentId,
                           Model model) {
        model.addAttribute("students", studentService.searchStudents(studentId));
        model.addAttribute("studentIdFilter", studentId);
        model.addAttribute("userStatuses", studentService.getAvailableStatuses());
        model.addAttribute("modalStudentId", modalStudentId);
        return "admin/students";
    }

    @PostMapping("/students")
    public String createStudent(@RequestParam String name,
                                @RequestParam String email,
                                @RequestParam String password,
                                @RequestParam(required = false) String course,
                                @RequestParam(required = false) String yearLevel,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String address,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate dateOfBirth,
                                @RequestParam(defaultValue = "ACTIVE") UserStatus status,
                                RedirectAttributes redirectAttributes) {
        try {
            authService.createStudentByAdmin(name, email, password, course, yearLevel, phone, address, dateOfBirth, status);
            redirectAttributes.addFlashAttribute("success", "Student account created successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/students";
    }

    @GetMapping("/students/{studentId}")
    public String studentDetails(@PathVariable String studentId, Model model) {
        populateStudentDetailModel(studentId, model);
        return "admin/student-detail";
    }

    @GetMapping("/students/{studentId}/modal")
    public String studentDetailsModal(@PathVariable String studentId, Model model) {
        populateStudentDetailModel(studentId, model);
        return "admin/student-detail-modal";
    }

    private void populateStudentDetailModel(String studentId, Model model) {
        Student student = studentService.getStudentByStudentId(studentId);
        List<IssueRecord> issueRecords = issueService.getStudentIssuesByStudentId(studentId);
        List<IssueRecord> activeIssues = issueRecords.stream()
                .filter(record -> !record.isReturned())
                .toList();
        long overdueItems = issueRecords.stream()
                .filter(record -> IssueStatus.OVERDUE.equals(record.getStatus()))
                .count();
        BigDecimal totalFineAmount = issueRecords.stream()
                .map(IssueRecord::getFineAmount)
                .filter(fine -> fine != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("student", student);
        model.addAttribute("issueRecords", issueRecords);
        model.addAttribute("activeIssues", activeIssues);
        model.addAttribute("activeCount", activeIssues.size());
        model.addAttribute("historyCount", issueRecords.size());
        model.addAttribute("overdueItems", overdueItems);
        model.addAttribute("totalFineAmount", totalFineAmount);
        model.addAttribute("userStatuses", studentService.getAvailableStatuses());
    }

    @PostMapping("/students/{studentId}/update")
    public String updateStudent(@PathVariable String studentId,
                                @RequestParam String name,
                                @RequestParam String email,
                                @RequestParam(required = false) String course,
                                @RequestParam(required = false) String yearLevel,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String address,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate dateOfBirth,
                                @RequestParam(defaultValue = "ACTIVE") UserStatus status,
                                RedirectAttributes redirectAttributes) {
        try {
            studentService.updateStudentByAdmin(studentId, name, email, course, yearLevel, phone, address, dateOfBirth, status);
            redirectAttributes.addFlashAttribute("success", "Student details updated successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/students?modalStudentId=" + studentId;
    }

    @PostMapping("/students/{studentId}/password")
    public String resetStudentPassword(@PathVariable String studentId,
                                       @RequestParam String newPassword,
                                       @RequestParam String confirmPassword,
                                       RedirectAttributes redirectAttributes) {
        try {
            studentService.resetStudentPassword(studentId, newPassword, confirmPassword);
            redirectAttributes.addFlashAttribute("success", "Student password reset successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/students?modalStudentId=" + studentId;
    }

    @PostMapping("/students/{studentId}/delete")
    public String deleteStudent(@PathVariable String studentId,
                                RedirectAttributes redirectAttributes) {
        try {
            studentService.deleteStudent(studentId);
            redirectAttributes.addFlashAttribute("success", "Student account deleted successfully.");
            return "redirect:/admin/students";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/admin/students?modalStudentId=" + studentId;
        }
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        User admin = adminService.getAdminByEmail(authentication.getName());
        model.addAttribute("adminUser", admin);
        model.addAttribute("transactionsManaged", issueService.countTransactionsManagedBy(authentication.getName()));
        model.addAttribute("activeCirculation", issueRecordRepository.countByStatus(IssueStatus.ISSUED) + issueRecordRepository.countByStatus(IssueStatus.OVERDUE));
        model.addAttribute("studentCount", userRepository.countByRole(Role.STUDENT));
        return "admin/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(Authentication authentication,
                                @RequestParam String name,
                                RedirectAttributes redirectAttributes) {
        try {
            adminService.updateProfile(authentication.getName(), name);
            redirectAttributes.addFlashAttribute("success", "Admin profile updated successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/profile";
    }

    @PostMapping("/profile/password")
    public String changePassword(Authentication authentication,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {
        try {
            adminService.changePassword(authentication.getName(), currentPassword, newPassword, confirmPassword);
            redirectAttributes.addFlashAttribute("success", "Password changed successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/profile";
    }
}
