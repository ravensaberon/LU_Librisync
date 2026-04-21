package com.lulibrisync.controller;

import com.lulibrisync.model.Fine;
import com.lulibrisync.model.FineStatus;
import com.lulibrisync.service.FineService;
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
import java.util.Locale;

@Controller
@RequestMapping("/admin/fines")
public class FineController {

    private final FineService fineService;

    public FineController(FineService fineService) {
        this.fineService = fineService;
    }

    @GetMapping
    public String fines(@RequestParam(required = false) FineStatus status,
                        @RequestParam(required = false) String studentKeyword,
                        Model model) {
        List<Fine> fineRecords = fineService.getAllFines().stream()
                .filter(fine -> status == null || fine.getStatus() == status)
                .filter(fine -> matchesStudentFilter(fine, studentKeyword))
                .toList();

        BigDecimal filteredOutstandingTotal = fineRecords.stream()
                .filter(fine -> FineStatus.UNPAID.equals(fine.getStatus()) || FineStatus.PARTIALLY_PAID.equals(fine.getStatus()))
                .map(Fine::getRemainingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("fines", fineRecords);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("studentKeyword", studentKeyword);
        model.addAttribute("fineStatuses", FineStatus.values());
        model.addAttribute("outstandingFineCount", fineService.countOutstandingFines());
        model.addAttribute("paidFineCount", fineService.countByStatus(FineStatus.PAID));
        model.addAttribute("waivedFineCount", fineService.countByStatus(FineStatus.WAIVED));
        model.addAttribute("outstandingFineTotal", fineService.getOutstandingFineTotal());
        model.addAttribute("paidFineTotal", fineService.getTotalAmountByStatus(FineStatus.PAID));
        model.addAttribute("waivedFineTotal", fineService.getTotalAmountByStatus(FineStatus.WAIVED));
        model.addAttribute("filteredOutstandingTotal", filteredOutstandingTotal);
        model.addAttribute("filteredFineCount", fineRecords.size());
        return "admin/fines";
    }

    @PostMapping("/{fineId}/pay-partial")
    public String recordPartialPayment(@PathVariable Long fineId,
                                       @RequestParam BigDecimal amount,
                                       @RequestParam String paymentMethod,
                                       @RequestParam String receiptNumber,
                                       @RequestParam(required = false) String remarks,
                                       Authentication authentication,
                                       RedirectAttributes redirectAttributes) {
        try {
            fineService.recordPayment(fineId, amount, paymentMethod, receiptNumber, authentication.getName(), remarks);
            redirectAttributes.addFlashAttribute("success", "Fine payment recorded successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/fines";
    }

    @PostMapping("/{fineId}/pay")
    public String markFinePaid(@PathVariable Long fineId,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            fineService.markFinePaid(fineId, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Fine marked as paid successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/fines";
    }

    @PostMapping("/{fineId}/waive")
    public String waiveFine(@PathVariable Long fineId,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            fineService.waiveFine(fineId, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Fine waived successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/fines";
    }

    private boolean matchesStudentFilter(Fine fine, String studentKeyword) {
        if (studentKeyword == null || studentKeyword.isBlank()) {
            return true;
        }

        String normalizedKeyword = studentKeyword.trim().toLowerCase(Locale.ENGLISH);
        return fine.getStudent().getStudentId().toLowerCase(Locale.ENGLISH).contains(normalizedKeyword)
                || fine.getStudent().getUser().getName().toLowerCase(Locale.ENGLISH).contains(normalizedKeyword)
                || fine.getStudent().getUser().getEmail().toLowerCase(Locale.ENGLISH).contains(normalizedKeyword);
    }
}
