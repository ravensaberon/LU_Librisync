package com.lulibrisync.controller;

import com.lulibrisync.service.BookService;
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

    public IssueController(IssueService issueService,
                           BookService bookService,
                           StudentService studentService) {
        this.issueService = issueService;
        this.bookService = bookService;
        this.studentService = studentService;
    }

    @GetMapping("/admin/issues")
    public String issues(@RequestParam(required = false) Long editId, Model model) {
        model.addAttribute("activeIssues", issueService.getActiveIssues());
        model.addAttribute("issueHistory", issueService.getAllIssues());
        model.addAttribute("availableBooks", bookService.getAvailableBooks());
        model.addAttribute("students", studentService.searchStudents(null));
        model.addAttribute("defaultDueDate", LocalDate.now().plusDays(7));
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
            issueService.issueBook(bookId, studentId, dueDate, authentication.getName(), remarks);
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
                              RedirectAttributes redirectAttributes) {
        try {
            issueService.updateIssue(issueId, dueDate, remarks);
            redirectAttributes.addFlashAttribute("success", "Issue record updated successfully.");
            return "redirect:/admin/issues";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/admin/issues?editId=" + issueId;
        }
    }

    @PostMapping("/admin/issues/{issueId}/return")
    public String returnBook(@PathVariable Long issueId, RedirectAttributes redirectAttributes) {
        try {
            issueService.returnBook(issueId);
            redirectAttributes.addFlashAttribute("success", "Book returned successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/issues";
    }

    @PostMapping("/admin/issues/{issueId}/delete")
    public String deleteIssue(@PathVariable Long issueId, RedirectAttributes redirectAttributes) {
        try {
            issueService.deleteIssue(issueId);
            redirectAttributes.addFlashAttribute("success", "Issue record deleted successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/issues";
    }
}
