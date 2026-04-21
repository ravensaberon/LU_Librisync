package com.lulibrisync.controller;

import com.lulibrisync.service.AuditLogService;
import com.lulibrisync.service.BookService;
import com.lulibrisync.service.CirculationPolicyService;
import com.lulibrisync.service.IssueService;
import com.lulibrisync.service.StudentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class IssueController {

    private final IssueService issueService;
    private final BookService bookService;
    private final StudentService studentService;
    private final AuditLogService auditLogService;
    private final CirculationPolicyService circulationPolicyService;

    public IssueController(IssueService issueService,
                           BookService bookService,
                           StudentService studentService,
                           AuditLogService auditLogService,
                           CirculationPolicyService circulationPolicyService) {
        this.issueService = issueService;
        this.bookService = bookService;
        this.studentService = studentService;
        this.auditLogService = auditLogService;
        this.circulationPolicyService = circulationPolicyService;
    }

    @GetMapping("/admin/issues")
    public String issues(@RequestParam(required = false) Long editId, Model model) {
        var activeIssues = issueService.getActiveIssues();
        var issueHistory = issueService.getAllIssues();
        model.addAttribute("activeIssues", activeIssues);
        model.addAttribute("issueHistory", issueHistory);
        model.addAttribute("availableBooks", bookService.getAvailableBooks());
        model.addAttribute("students", studentService.searchStudents(null));
        model.addAttribute("defaultDueDate", LocalDate.now().plusDays(7));
        model.addAttribute("activeIssueCount", activeIssues.size());
        model.addAttribute("historyCount", issueHistory.size());
        model.addAttribute("overdueIssueCount", activeIssues.stream().filter(issue -> issue.getStatus().name().equals("OVERDUE")).count());
        model.addAttribute("maxLoanDays", circulationPolicyService.getMaxLoanDays());
        model.addAttribute("maxActiveLoans", circulationPolicyService.getMaxActiveLoans());
        if (editId != null) {
            var editIssue = issueService.getIssueById(editId);
            model.addAttribute("editIssue", editIssue);
            model.addAttribute("editIssueDueDate", editIssue.getDueDate().toLocalDate());
        }
        return "issues/manage";
    }

    @PostMapping("/admin/issues")
    public String issueBook(@RequestParam Long bookId,
                            @RequestParam Long studentId,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
                            @RequestParam(required = false) String remarks,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            var issueRecord = issueService.issueBook(bookId, studentId, dueDate, authentication.getName(), remarks);
            auditLogService.log(
                    authentication.getName(),
                    "BOOK_ISSUED",
                    "ISSUE_RECORD",
                    issueRecord.getId().toString(),
                    "Book issued",
                    "Issue code: " + issueRecord.getQrIssueCode() + " | Borrower: " + issueRecord.getStudent().getStudentId()
            );
            redirectAttributes.addFlashAttribute("success", "Book issued successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/issues";
    }

    @PostMapping("/admin/issues/{issueId}/update")
    public String updateIssue(@PathVariable Long issueId,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
                              @RequestParam(required = false) String remarks,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            var issueRecord = issueService.updateIssue(issueId, dueDate, remarks);
            auditLogService.log(
                    authentication.getName(),
                    "ISSUE_UPDATED",
                    "ISSUE_RECORD",
                    issueId.toString(),
                    "Issue record updated",
                    "Due date: " + issueRecord.getDueDate() + " | Fine: " + issueRecord.getFineAmount()
            );
            redirectAttributes.addFlashAttribute("success", "Issue record updated successfully.");
            return "redirect:/admin/issues";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/admin/issues?editId=" + issueId;
        }
    }

    @PostMapping("/admin/issues/{issueId}/return")
    public String returnBook(@PathVariable Long issueId,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            var issueRecord = issueService.returnBook(issueId);
            auditLogService.log(
                    authentication.getName(),
                    "BOOK_RETURNED",
                    "ISSUE_RECORD",
                    issueId.toString(),
                    "Book returned",
                    "Issue code: " + issueRecord.getQrIssueCode() + " | Fine: " + issueRecord.getFineAmount()
            );
            redirectAttributes.addFlashAttribute("success", "Book returned successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/issues";
    }

    @PostMapping("/student/issues/{issueId}/return")
    public String returnBookByStudent(@PathVariable Long issueId,
                                      @RequestParam(defaultValue = "/student/history") String redirectTo,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        try {
            var issueRecord = issueService.returnBookByStudent(issueId, authentication.getName());
            auditLogService.log(
                    authentication.getName(),
                    "SELF_SERVICE_RETURN",
                    "ISSUE_RECORD",
                    issueId.toString(),
                    "Student returned a borrowed book",
                    "Issue code: " + issueRecord.getQrIssueCode() + " | Fine: " + issueRecord.getFineAmount()
            );
            redirectAttributes.addFlashAttribute("success", "Book returned successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:" + resolveStudentRedirect(redirectTo);
    }

    @PostMapping("/admin/issues/{issueId}/delete")
    public String deleteIssue(@PathVariable Long issueId,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            var issueRecord = issueService.getIssueById(issueId);
            issueService.deleteIssue(issueId);
            auditLogService.log(
                    authentication.getName(),
                    "ISSUE_DELETED",
                    "ISSUE_RECORD",
                    issueId.toString(),
                    "Issue record deleted",
                    "Issue code: " + issueRecord.getQrIssueCode()
            );
            redirectAttributes.addFlashAttribute("success", "Issue record deleted successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/issues";
    }

    private String resolveStudentRedirect(String redirectTo) {
        if (redirectTo == null || redirectTo.isBlank()) {
            return "/student/history";
        }
        if (redirectTo.startsWith("/student/")) {
            return redirectTo;
        }
        return "/student/history";
    }
}
