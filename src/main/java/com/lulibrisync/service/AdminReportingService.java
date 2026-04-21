package com.lulibrisync.service;

import com.lulibrisync.model.AuditLog;
import com.lulibrisync.model.Fine;
import com.lulibrisync.model.IssueRecord;
import com.lulibrisync.model.IssueStatus;
import com.lulibrisync.model.Reservation;
import com.lulibrisync.model.ReservationStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class AdminReportingService {

    private static final DateTimeFormatter CSV_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    private final IssueService issueService;
    private final FineService fineService;
    private final ReservationService reservationService;
    private final AuditLogService auditLogService;

    public AdminReportingService(IssueService issueService,
                                 FineService fineService,
                                 ReservationService reservationService,
                                 AuditLogService auditLogService) {
        this.issueService = issueService;
        this.fineService = fineService;
        this.reservationService = reservationService;
        this.auditLogService = auditLogService;
    }

    public List<IssueRecord> getCirculationRecords(LocalDate dateFrom, LocalDate dateTo) {
        return issueService.getAllIssues().stream()
                .filter(issue -> withinRange(issue.getIssueDate(), dateFrom, dateTo))
                .toList();
    }

    public List<IssueRecord> getOverdueRecords(LocalDate dateFrom, LocalDate dateTo) {
        return issueService.getAllIssues().stream()
                .filter(issue -> IssueStatus.OVERDUE.equals(issue.getStatus()))
                .filter(issue -> withinRange(issue.getDueDate(), dateFrom, dateTo))
                .toList();
    }

    public List<Fine> getFineRecords(LocalDate dateFrom, LocalDate dateTo) {
        return fineService.getAllFines().stream()
                .filter(fine -> withinRange(fine.getCalculatedAt(), dateFrom, dateTo))
                .toList();
    }

    public List<Reservation> getReservationRecords(LocalDate dateFrom, LocalDate dateTo) {
        return reservationService.getAllReservations().stream()
                .filter(reservation -> withinRange(reservation.getReservedAt(), dateFrom, dateTo))
                .toList();
    }

    public List<AuditLog> getAuditRecords(LocalDate dateFrom, LocalDate dateTo) {
        return auditLogService.getAllLogs().stream()
                .filter(log -> withinRange(log.getCreatedAt(), dateFrom, dateTo))
                .toList();
    }

    public byte[] buildCsv(String reportType, LocalDate dateFrom, LocalDate dateTo) {
        String normalizedType = reportType == null ? "" : reportType.trim().toLowerCase(Locale.ENGLISH);

        return switch (normalizedType) {
            case "circulation" -> buildCirculationCsv(getCirculationRecords(dateFrom, dateTo));
            case "overdue" -> buildOverdueCsv(getOverdueRecords(dateFrom, dateTo));
            case "fines" -> buildFinesCsv(getFineRecords(dateFrom, dateTo));
            case "reservations" -> buildReservationsCsv(getReservationRecords(dateFrom, dateTo));
            case "audit" -> buildAuditCsv(getAuditRecords(dateFrom, dateTo));
            default -> throw new IllegalArgumentException("Unsupported report type.");
        };
    }

    private byte[] buildCirculationCsv(List<IssueRecord> issues) {
        StringBuilder builder = new StringBuilder();
        builder.append("Issue Code,Book,Student ID,Student Name,Issued By,Issue Date,Due Date,Return Date,Status,Fine Amount,Remarks\n");
        for (IssueRecord issue : issues) {
            builder.append(csv(issue.getQrIssueCode())).append(',')
                    .append(csv(issue.getBook().getTitle())).append(',')
                    .append(csv(issue.getStudent().getStudentId())).append(',')
                    .append(csv(issue.getStudent().getUser().getName())).append(',')
                    .append(csv(issue.getIssuedBy().getName())).append(',')
                    .append(csv(formatDateTime(issue.getIssueDate()))).append(',')
                    .append(csv(formatDateTime(issue.getDueDate()))).append(',')
                    .append(csv(formatDateTime(issue.getReturnDate()))).append(',')
                    .append(csv(issue.getStatus().name())).append(',')
                    .append(csv(String.valueOf(issue.getFineAmount()))).append(',')
                    .append(csv(issue.getRemarks())).append('\n');
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] buildOverdueCsv(List<IssueRecord> overdueIssues) {
        StringBuilder builder = new StringBuilder();
        builder.append("Issue Code,Book,Student ID,Student Name,Due Date,Days Overdue,Fine Amount,Issued By\n");
        LocalDateTime now = LocalDateTime.now();
        for (IssueRecord issue : overdueIssues) {
            long daysOverdue = issue.getDueDate() == null ? 0L : Math.max(1L, java.time.Duration.between(issue.getDueDate(), now).toDays());
            builder.append(csv(issue.getQrIssueCode())).append(',')
                    .append(csv(issue.getBook().getTitle())).append(',')
                    .append(csv(issue.getStudent().getStudentId())).append(',')
                    .append(csv(issue.getStudent().getUser().getName())).append(',')
                    .append(csv(formatDateTime(issue.getDueDate()))).append(',')
                    .append(csv(String.valueOf(daysOverdue))).append(',')
                    .append(csv(String.valueOf(issue.getFineAmount()))).append(',')
                    .append(csv(issue.getIssuedBy().getName())).append('\n');
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] buildFinesCsv(List<Fine> fines) {
        StringBuilder builder = new StringBuilder();
        builder.append("Fine ID,Student ID,Student Name,Issue Code,Book,Amount,Status,Calculated At,Paid Or Waived At\n");
        for (Fine fine : fines) {
            builder.append(csv(String.valueOf(fine.getId()))).append(',')
                    .append(csv(fine.getStudent().getStudentId())).append(',')
                    .append(csv(fine.getStudent().getUser().getName())).append(',')
                    .append(csv(fine.getIssueRecord().getQrIssueCode())).append(',')
                    .append(csv(fine.getIssueRecord().getBook().getTitle())).append(',')
                    .append(csv(String.valueOf(fine.getAmount()))).append(',')
                    .append(csv(fine.getStatus().name())).append(',')
                    .append(csv(formatDateTime(fine.getCalculatedAt()))).append(',')
                    .append(csv(formatDateTime(fine.getPaidAt()))).append('\n');
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] buildReservationsCsv(List<Reservation> reservations) {
        List<Reservation> sortedReservations = reservations.stream()
                .sorted(Comparator.comparing(Reservation::getReservedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();

        StringBuilder builder = new StringBuilder();
        builder.append("Reservation ID,Book,Student ID,Student Name,Queue Position,Status,Reserved At,Claim Until\n");
        for (Reservation reservation : sortedReservations) {
            builder.append(csv(String.valueOf(reservation.getId()))).append(',')
                    .append(csv(reservation.getBook().getTitle())).append(',')
                    .append(csv(reservation.getStudent().getStudentId())).append(',')
                    .append(csv(reservation.getStudent().getUser().getName())).append(',')
                    .append(csv(String.valueOf(reservation.getQueuePosition()))).append(',')
                    .append(csv(reservation.getStatus().name())).append(',')
                    .append(csv(formatDateTime(reservation.getReservedAt()))).append(',')
                    .append(csv(formatDateTime(reservation.getExpiresAt()))).append('\n');
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] buildAuditCsv(List<AuditLog> logs) {
        StringBuilder builder = new StringBuilder();
        builder.append("Log ID,Timestamp,Actor Name,Actor Email,Action,Entity Type,Entity ID,Summary,Details\n");
        for (AuditLog log : logs) {
            builder.append(csv(String.valueOf(log.getId()))).append(',')
                    .append(csv(formatDateTime(log.getCreatedAt()))).append(',')
                    .append(csv(log.getActorName())).append(',')
                    .append(csv(log.getActorEmail())).append(',')
                    .append(csv(log.getAction())).append(',')
                    .append(csv(log.getEntityType())).append(',')
                    .append(csv(log.getEntityId())).append(',')
                    .append(csv(log.getSummary())).append(',')
                    .append(csv(log.getDetails())).append('\n');
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private boolean withinRange(LocalDateTime value, LocalDate dateFrom, LocalDate dateTo) {
        if (value == null) {
            return false;
        }
        LocalDateTime rangeStart = dateFrom == null ? null : dateFrom.atStartOfDay();
        LocalDateTime rangeEnd = dateTo == null ? null : dateTo.atTime(LocalTime.MAX);
        if (rangeStart != null && value.isBefore(rangeStart)) {
            return false;
        }
        if (rangeEnd != null && value.isAfter(rangeEnd)) {
            return false;
        }
        return true;
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : CSV_DATE_TIME_FORMATTER.format(value);
    }

    private String csv(String value) {
        String normalized = value == null ? "" : value;
        return "\"" + normalized.replace("\"", "\"\"") + "\"";
    }
}
