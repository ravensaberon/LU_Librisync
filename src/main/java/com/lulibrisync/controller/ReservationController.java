package com.lulibrisync.controller;

import com.lulibrisync.service.AuditLogService;
import com.lulibrisync.service.CirculationPolicyService;
import com.lulibrisync.service.IssueService;
import com.lulibrisync.service.ReservationService;
import com.lulibrisync.service.StudentService;
import com.lulibrisync.model.ReservationStatus;
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

import java.time.LocalDate;

@Controller
public class ReservationController {

    private final ReservationService reservationService;
    private final IssueService issueService;
    private final AuditLogService auditLogService;
    private final StudentService studentService;
    private final CirculationPolicyService circulationPolicyService;

    public ReservationController(ReservationService reservationService,
                                 IssueService issueService,
                                 AuditLogService auditLogService,
                                 StudentService studentService,
                                 CirculationPolicyService circulationPolicyService) {
        this.reservationService = reservationService;
        this.issueService = issueService;
        this.auditLogService = auditLogService;
        this.studentService = studentService;
        this.circulationPolicyService = circulationPolicyService;
    }

    @GetMapping("/student/reservations")
    public String studentReservations(Authentication authentication, Model model) {
        reservationService.syncReadyReservations();
        model.addAttribute("reservations", reservationService.getStudentReservations(authentication.getName()));
        model.addAttribute("defaultBorrowDueDate", LocalDate.now().plusDays(circulationPolicyService.getMaxLoanDays()));
        model.addAttribute("maxLoanDays", circulationPolicyService.getMaxLoanDays());
        return "student/reservations";
    }

    @PostMapping("/student/reservations")
    public String placeReservation(@RequestParam Long bookId,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            var reservation = reservationService.placeReservation(bookId, authentication.getName());
            auditLogService.log(
                    authentication.getName(),
                    "RESERVATION_CREATED",
                    "RESERVATION",
                    reservation.getId().toString(),
                    "Reservation placed",
                    "Book: " + reservation.getBook().getTitle() + " | Queue: " + reservation.getQueuePosition()
            );
            redirectAttributes.addFlashAttribute("success", "Book reserved successfully. You will be notified once it becomes available.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/student/catalog";
    }

    @PostMapping("/student/reservations/{reservationId}/claim")
    public String claimStudentReservation(@PathVariable Long reservationId,
                                          Authentication authentication,
                                          RedirectAttributes redirectAttributes) {
        try {
            var reservation = reservationService.getReservationById(reservationId);
            var student = studentService.getStudentByEmail(authentication.getName());
            if (!reservation.getStudent().getId().equals(student.getId())) {
                throw new IllegalArgumentException("You can only claim your own reservation.");
            }
            if (!ReservationStatus.READY.equals(reservation.getStatus())) {
                throw new IllegalArgumentException("This reservation is not ready to claim yet.");
            }

            LocalDate dueDate = LocalDate.now().plusDays(circulationPolicyService.getMaxLoanDays());
            var issueRecord = issueService.issueBook(reservation.getBook().getId(), student.getId(), dueDate, authentication.getName(), "Self-service reservation claim");
            auditLogService.log(
                    authentication.getName(),
                    "SELF_SERVICE_RESERVATION_CLAIM",
                    "RESERVATION",
                    reservationId.toString(),
                    "Student claimed a ready reservation",
                    "Reservation " + reservationId + " issued as " + issueRecord.getQrIssueCode()
            );
            redirectAttributes.addFlashAttribute("success", "Reserved book borrowed successfully. Due on " + dueDate + ".");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/student/reservations";
    }

    @PostMapping("/student/reservations/{reservationId}/cancel")
    public String cancelStudentReservation(@PathVariable Long reservationId,
                                           Authentication authentication,
                                           RedirectAttributes redirectAttributes) {
        try {
            reservationService.cancelReservationByStudent(reservationId, authentication.getName());
            auditLogService.log(
                    authentication.getName(),
                    "RESERVATION_CANCELLED",
                    "RESERVATION",
                    reservationId.toString(),
                    "Reservation cancelled by student",
                    "Student cancelled reservation " + reservationId + "."
            );
            redirectAttributes.addFlashAttribute("success", "Reservation cancelled successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/student/reservations";
    }

    @GetMapping("/admin/reservations")
    public String adminReservations(Model model) {
        reservationService.syncReadyReservations();
        var reservations = reservationService.getAllReservations();
        model.addAttribute("reservations", reservations);
        model.addAttribute("defaultDueDate", LocalDate.now().plusDays(7));
        model.addAttribute("reservationCount", reservations.size());
        model.addAttribute("pendingReservationCount", reservations.stream().filter(reservation -> reservation.getStatus().name().equals("PENDING")).count());
        model.addAttribute("readyReservationCount", reservations.stream().filter(reservation -> reservation.getStatus().name().equals("READY")).count());
        return "admin/reservations";
    }

    @PostMapping("/admin/reservations/{reservationId}/claim")
    public String claimReservation(@PathVariable Long reservationId,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
                                   @RequestParam(required = false) String remarks,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            var reservation = reservationService.getReservationById(reservationId);
            var issueRecord = issueService.issueBook(reservation.getBook().getId(), reservation.getStudent().getId(), dueDate, authentication.getName(), remarks);
            auditLogService.log(
                    authentication.getName(),
                    "RESERVATION_CLAIMED",
                    "RESERVATION",
                    reservationId.toString(),
                    "Reserved book issued",
                    "Reservation claimed and issued as " + issueRecord.getQrIssueCode()
            );
            redirectAttributes.addFlashAttribute("success", "Reserved book issued successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/reservations";
    }

    @PostMapping("/admin/reservations/{reservationId}/cancel")
    public String cancelAdminReservation(@PathVariable Long reservationId,
                                         Authentication authentication,
                                         RedirectAttributes redirectAttributes) {
        try {
            reservationService.cancelReservationByAdmin(reservationId);
            auditLogService.log(
                    authentication.getName(),
                    "RESERVATION_CANCELLED",
                    "RESERVATION",
                    reservationId.toString(),
                    "Reservation cancelled by admin",
                    "Reservation " + reservationId + " was cancelled by admin."
            );
            redirectAttributes.addFlashAttribute("success", "Reservation cancelled successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/reservations";
    }
}
