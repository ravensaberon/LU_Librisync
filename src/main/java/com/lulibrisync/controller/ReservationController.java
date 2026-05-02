package com.lulibrisync.controller;

import com.lulibrisync.service.AuditLogService;
import com.lulibrisync.service.CirculationPolicyService;
import com.lulibrisync.service.IssueService;
import com.lulibrisync.service.ReservationService;
import com.lulibrisync.service.StudentService;
import com.lulibrisync.util.PaginationUtils;
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

    private static final int RESERVATION_SECTION_PAGE_SIZE = 8;

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
    public String studentReservations(Authentication authentication,
                                      @RequestParam(defaultValue = "borrow") String tab,
                                      @RequestParam(defaultValue = "1") Integer borrowPage,
                                      @RequestParam(defaultValue = "1") Integer queuePage,
                                      Model model) {
        reservationService.syncReadyReservations();
        var borrowRequests = reservationService.getStudentBorrowRequests(authentication.getName());
        var queueReservations = reservationService.getStudentQueueReservations(authentication.getName());
        var borrowRequestsPage = PaginationUtils.paginate(borrowRequests, borrowPage, RESERVATION_SECTION_PAGE_SIZE);
        var queueReservationsPage = PaginationUtils.paginate(queueReservations, queuePage, RESERVATION_SECTION_PAGE_SIZE);
        model.addAttribute("borrowRequests", borrowRequestsPage.getItems());
        model.addAttribute("borrowRequestsPage", borrowRequestsPage);
        model.addAttribute("queueReservations", queueReservationsPage.getItems());
        model.addAttribute("queueReservationsPage", queueReservationsPage);
        model.addAttribute("borrowRequestWindowMinutes", reservationService.getBorrowRequestWindowMinutes());
        model.addAttribute("reservationScheduleMaxDate", LocalDate.now().plusDays(reservationService.getMaxPreferredPickupDays()));
        model.addAttribute("activeTab", "queue".equalsIgnoreCase(tab) ? "queue" : "borrow");
        return "student/reservations";
    }

    @PostMapping("/student/reservations")
    public String placeReservation(@RequestParam Long bookId,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate preferredPickupDate,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            var reservation = reservationService.placeReservation(bookId, authentication.getName(), preferredPickupDate);
            auditLogService.log(
                    authentication.getName(),
                    "RESERVATION_CREATED",
                    "RESERVATION",
                    reservation.getId().toString(),
                    "Reservation placed",
                    "Book: " + reservation.getBook().getTitle()
                            + " | Queue: " + reservation.getQueuePosition()
                            + " | Preferred pickup: " + reservation.getPreferredPickupDate()
            );
            if (ReservationStatus.READY.equals(reservation.getStatus())) {
                redirectAttributes.addFlashAttribute("success", "Reservation placed. A copy is now ready for desk pickup and staff release.");
            } else {
                redirectAttributes.addFlashAttribute("success", "Reservation placed for " + reservation.getPreferredPickupDate() + ".");
            }
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/student/catalog";
    }

    @PostMapping("/student/reservations/{reservationId}/claim")
    public String claimStudentReservation(@PathVariable Long reservationId,
                                          @RequestParam(defaultValue = "borrow") String tab,
                                          @RequestParam(defaultValue = "1") Integer borrowPage,
                                          @RequestParam(defaultValue = "1") Integer queuePage,
                                          Authentication authentication,
                                          RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("info", "Please proceed to the circulation desk. Staff must confirm the physical pickup before the book is issued to your account.");
        return "redirect:/student/reservations?tab=" + ("queue".equalsIgnoreCase(tab) ? "queue" : "borrow")
                + "&borrowPage=" + Math.max(1, borrowPage)
                + "&queuePage=" + Math.max(1, queuePage);
    }

    @PostMapping("/student/reservations/{reservationId}/cancel")
    public String cancelStudentReservation(@PathVariable Long reservationId,
                                           @RequestParam(defaultValue = "borrow") String tab,
                                           @RequestParam(defaultValue = "1") Integer borrowPage,
                                           @RequestParam(defaultValue = "1") Integer queuePage,
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
        return "redirect:/student/reservations?tab=" + ("queue".equalsIgnoreCase(tab) ? "queue" : "borrow")
                + "&borrowPage=" + Math.max(1, borrowPage)
                + "&queuePage=" + Math.max(1, queuePage);
    }

    @GetMapping("/admin/reservations")
    public String adminReservations(@RequestParam(defaultValue = "1") Integer borrowPage,
                                    @RequestParam(defaultValue = "1") Integer queuePage,
                                    Model model) {
        reservationService.syncReadyReservations();
        var borrowRequests = reservationService.getBorrowRequests();
        var queueReservations = reservationService.getQueueReservations();
        var borrowRequestsPage = PaginationUtils.paginate(borrowRequests, borrowPage, RESERVATION_SECTION_PAGE_SIZE);
        var queueReservationsPage = PaginationUtils.paginate(queueReservations, queuePage, RESERVATION_SECTION_PAGE_SIZE);
        model.addAttribute("borrowRequests", borrowRequestsPage.getItems());
        model.addAttribute("borrowRequestsPage", borrowRequestsPage);
        model.addAttribute("queueReservations", queueReservationsPage.getItems());
        model.addAttribute("queueReservationsPage", queueReservationsPage);
        model.addAttribute("defaultDueDate", LocalDate.now().plusDays(7));
        model.addAttribute("reservationCount", borrowRequests.size() + queueReservations.size());
        model.addAttribute("borrowRequestCount", borrowRequests.stream().filter(reservation -> reservation.getStatus() == ReservationStatus.READY).count());
        model.addAttribute("pendingReservationCount", queueReservations.stream().filter(reservation -> reservation.getStatus().name().equals("PENDING")).count());
        model.addAttribute("readyReservationCount", borrowRequests.stream().filter(reservation -> reservation.getStatus().name().equals("READY")).count()
                + queueReservations.stream().filter(reservation -> reservation.getStatus().name().equals("READY")).count());
        model.addAttribute("borrowRequestWindowMinutes", reservationService.getBorrowRequestWindowMinutes());
        return "admin/reservations";
    }

    @PostMapping("/admin/reservations/{reservationId}/claim")
    public String claimReservation(@PathVariable Long reservationId,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
                                   @RequestParam(required = false) String remarks,
                                   @RequestParam(defaultValue = "1") Integer borrowPage,
                                   @RequestParam(defaultValue = "1") Integer queuePage,
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
                    "Reserved book issued by staff",
                    "Reservation claimed and issued as " + issueRecord.getQrIssueCode()
            );
            redirectAttributes.addFlashAttribute("success", "Reserved book released and issued successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/reservations?borrowPage=" + Math.max(1, borrowPage) + "&queuePage=" + Math.max(1, queuePage);
    }

    @PostMapping("/admin/reservations/{reservationId}/cancel")
    public String cancelAdminReservation(@PathVariable Long reservationId,
                                         @RequestParam(defaultValue = "1") Integer borrowPage,
                                         @RequestParam(defaultValue = "1") Integer queuePage,
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
        return "redirect:/admin/reservations?borrowPage=" + Math.max(1, borrowPage) + "&queuePage=" + Math.max(1, queuePage);
    }
}
