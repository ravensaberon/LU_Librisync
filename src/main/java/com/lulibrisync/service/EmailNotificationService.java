package com.lulibrisync.service;

import com.lulibrisync.model.EmailNotification;
import com.lulibrisync.model.EmailNotificationStatus;
import com.lulibrisync.model.EmailNotificationType;
import com.lulibrisync.model.IssueRecord;
import com.lulibrisync.model.IssueStatus;
import com.lulibrisync.model.Reservation;
import com.lulibrisync.model.ReservationStatus;
import com.lulibrisync.model.User;
import com.lulibrisync.repository.EmailNotificationRepository;
import com.lulibrisync.repository.IssueRecordRepository;
import com.lulibrisync.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class EmailNotificationService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a", Locale.ENGLISH);

    private final EmailNotificationRepository emailNotificationRepository;
    private final IssueRecordRepository issueRecordRepository;
    private final ReservationRepository reservationRepository;
    private final Path outboxRoot;
    private final String smtpHost;
    private final String smtpPort;
    private final String smtpUsername;
    private final String smtpPassword;
    private final String smtpFrom;
    private final String smtpSsl;

    public EmailNotificationService(EmailNotificationRepository emailNotificationRepository,
                                    IssueRecordRepository issueRecordRepository,
                                    ReservationRepository reservationRepository,
                                    @Value("${lulibrisync.smtp.host:}") String smtpHost,
                                    @Value("${lulibrisync.smtp.port:587}") String smtpPort,
                                    @Value("${lulibrisync.smtp.username:lulibrisync@gmail.com}") String smtpUsername,
                                    @Value("${lulibrisync.smtp.password:}") String smtpPassword,
                                    @Value("${lulibrisync.smtp.from:lulibrisync@gmail.com}") String smtpFrom,
                                    @Value("${lulibrisync.smtp.ssl:true}") String smtpSsl,
                                    @Value("${lulibrisync.notification.outbox-root:${lulibrisync.storage.root:${user.dir}/storage}/email-outbox}") String outboxRootPath) {
        this.emailNotificationRepository = emailNotificationRepository;
        this.issueRecordRepository = issueRecordRepository;
        this.reservationRepository = reservationRepository;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
        this.smtpFrom = smtpFrom;
        this.smtpSsl = smtpSsl;
        this.outboxRoot = Path.of(outboxRootPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.outboxRoot);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to initialize email notification outbox.", exception);
        }
    }

    @Transactional
    public void queueDueReminder(IssueRecord issueRecord) {
        if (issueRecord == null || issueRecord.getStudent() == null || issueRecord.getStudent().getUser() == null) {
            return;
        }

        User recipient = issueRecord.getStudent().getUser();
        String subject = "Due Reminder | Issue #" + issueRecord.getId() + " | " + issueRecord.getBook().getTitle();
        String body = """
                Hello %s,

                This is a reminder from LU Librisync that your borrowed book is due soon.

                Book title: %s
                Student ID: %s
                Due date: %s
                Issue code: %s

                Please return the book on or before the due date to avoid additional fines.
                """.formatted(
                recipient.getName(),
                issueRecord.getBook().getTitle(),
                issueRecord.getStudent().getStudentId(),
                DATE_TIME_FORMATTER.format(issueRecord.getDueDate()),
                issueRecord.getQrIssueCode()
        );

        LocalDateTime scheduledAt = issueRecord.getDueDate().minusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0);
        if (scheduledAt.isBefore(LocalDateTime.now())) {
            scheduledAt = LocalDateTime.now().plusMinutes(1);
        }

        upsertPendingNotification(recipient, EmailNotificationType.DUE_REMINDER, subject, body, scheduledAt);
    }

    @Transactional
    public void cancelDueReminder(IssueRecord issueRecord) {
        if (issueRecord == null || issueRecord.getStudent() == null || issueRecord.getStudent().getUser() == null) {
            return;
        }
        String subject = "Due Reminder | Issue #" + issueRecord.getId() + " | " + issueRecord.getBook().getTitle();
        emailNotificationRepository.findByUser_IdAndNotificationTypeAndSubjectAndStatus(
                        issueRecord.getStudent().getUser().getId(),
                        EmailNotificationType.DUE_REMINDER,
                        subject,
                        EmailNotificationStatus.PENDING
                )
                .ifPresent(emailNotificationRepository::delete);
    }

    @Transactional
    public void queueReservationReadyNotification(Reservation reservation) {
        if (reservation == null || reservation.getStudent() == null || reservation.getStudent().getUser() == null) {
            return;
        }

        User recipient = reservation.getStudent().getUser();
        String subject = "Reservation Ready | Reservation #" + reservation.getId() + " | " + reservation.getBook().getTitle();
        String body = """
                Hello %s,

                Your reserved library book is now ready for claiming.

                Book title: %s
                Student ID: %s
                Queue position: %s
                Claim until: %s

                Please visit the library before the claim window expires.
                """.formatted(
                recipient.getName(),
                reservation.getBook().getTitle(),
                reservation.getStudent().getStudentId(),
                reservation.getQueuePosition(),
                reservation.getExpiresAt() == null ? "As soon as possible" : DATE_TIME_FORMATTER.format(reservation.getExpiresAt())
        );

        upsertPendingNotification(recipient, EmailNotificationType.RESERVATION_READY, subject, body, LocalDateTime.now().plusMinutes(1));
    }

    @Transactional
    public void cancelReservationReadyNotification(Reservation reservation) {
        if (reservation == null || reservation.getStudent() == null || reservation.getStudent().getUser() == null) {
            return;
        }
        String subject = "Reservation Ready | Reservation #" + reservation.getId() + " | " + reservation.getBook().getTitle();
        emailNotificationRepository.findByUser_IdAndNotificationTypeAndSubjectAndStatus(
                        reservation.getStudent().getUser().getId(),
                        EmailNotificationType.RESERVATION_READY,
                        subject,
                        EmailNotificationStatus.PENDING
                )
                .ifPresent(emailNotificationRepository::delete);
    }

    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void processPendingNotifications() {
        ensureReminderQueueCoverage();

        List<EmailNotification> dueNotifications = emailNotificationRepository
                .findTop25ByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(EmailNotificationStatus.PENDING, LocalDateTime.now());

        for (EmailNotification notification : dueNotifications) {
            boolean sent = sendEmail(notification.getUser().getEmail(), notification.getSubject(), notification.getBody());
            notification.setSentAt(LocalDateTime.now());
            notification.setStatus(sent ? EmailNotificationStatus.SENT : EmailNotificationStatus.FAILED);
            emailNotificationRepository.save(notification);
        }
    }

    public boolean sendImmediateEmail(String toEmail, String subject, String body) {
        return sendEmail(toEmail, subject, body);
    }

    public boolean sendImmediateHtmlEmail(String toEmail, String subject, String body) {
        return sendEmail(toEmail, subject, body, true);
    }

    private void ensureReminderQueueCoverage() {
        List<IssueRecord> activeIssues = issueRecordRepository.findByStatusInOrderByIssueDateDesc(List.of(IssueStatus.ISSUED, IssueStatus.OVERDUE));
        for (IssueRecord issueRecord : activeIssues) {
            if (!issueRecord.isReturned()) {
                queueDueReminder(issueRecord);
            }
        }

        List<Reservation> readyReservations = reservationRepository.findByStatusInOrderByReservedAtAsc(List.of(ReservationStatus.READY));
        for (Reservation reservation : readyReservations) {
            queueReservationReadyNotification(reservation);
        }
    }

    private void upsertPendingNotification(User recipient,
                                           EmailNotificationType notificationType,
                                           String subject,
                                           String body,
                                           LocalDateTime scheduledAt) {
        EmailNotification notification = emailNotificationRepository
                .findTopByUser_IdAndNotificationTypeAndSubjectOrderByCreatedAtDesc(
                        recipient.getId(),
                        notificationType,
                        subject
                )
                .orElseGet(EmailNotification::new);

        if (notification.getId() != null
                && notificationType.equals(notification.getNotificationType())
                && recipient.getId().equals(notification.getUser().getId())
                && subject.equals(notification.getSubject())
                && body.equals(notification.getBody())
                && scheduledAt.equals(notification.getScheduledAt())
                && (EmailNotificationStatus.PENDING.equals(notification.getStatus())
                || EmailNotificationStatus.SENT.equals(notification.getStatus())
                || EmailNotificationStatus.FAILED.equals(notification.getStatus()))) {
            return;
        }

        notification.setUser(recipient);
        notification.setNotificationType(notificationType);
        notification.setSubject(subject);
        notification.setBody(body);
        notification.setScheduledAt(scheduledAt);
        notification.setStatus(EmailNotificationStatus.PENDING);
        notification.setSentAt(null);
        emailNotificationRepository.save(notification);
    }

    private boolean sendEmail(String toEmail, String subject, String body) {
        return sendEmail(toEmail, subject, body, false);
    }

    private boolean sendEmail(String toEmail, String subject, String body, boolean htmlBody) {
        if (!StringUtils.hasText(smtpHost) || !StringUtils.hasText(smtpFrom)) {
            writeOutboxCopy(toEmail, subject, body, htmlBody);
            return false;
        }

        String script = buildPowerShellMailScript(smtpHost, smtpPort, smtpUsername, smtpPassword, smtpFrom, smtpSsl, toEmail, subject, body, htmlBody);
        ProcessBuilder processBuilder = new ProcessBuilder("powershell", "-NoProfile", "-Command", script);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return true;
            }
            writeOutboxCopy(
                    toEmail,
                    subject,
                    body + System.lineSeparator() + System.lineSeparator() + "Send error:" + System.lineSeparator() + output,
                    htmlBody
            );
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            writeOutboxCopy(toEmail, subject, body, htmlBody);
        }
        return false;
    }

    private String buildPowerShellMailScript(String smtpHost,
                                             String smtpPort,
                                             String smtpUsername,
                                             String smtpPassword,
                                             String smtpFrom,
                                             String smtpSsl,
                                             String toEmail,
                                             String subject,
                                             String body,
                                             boolean htmlBody) {
        String credentialsBlock = "";
        if (StringUtils.hasText(smtpUsername)) {
            credentialsBlock = "$client.Credentials = New-Object System.Net.NetworkCredential('" + escapePowerShell(smtpUsername) + "', '" + escapePowerShell(smtpPassword) + "');";
        }

        return """
                $message = New-Object System.Net.Mail.MailMessage;
                $message.From = '%s';
                $message.To.Add('%s');
                $message.Subject = '%s';
                $message.Body = '%s';
                $message.IsBodyHtml = [System.Convert]::ToBoolean('%s');
                $client = New-Object System.Net.Mail.SmtpClient('%s', %s);
                $client.EnableSsl = [System.Convert]::ToBoolean('%s');
                %s
                $client.Send($message);
                """.formatted(
                escapePowerShell(smtpFrom),
                escapePowerShell(toEmail),
                escapePowerShell(subject),
                escapePowerShell(body),
                htmlBody,
                escapePowerShell(smtpHost),
                escapePowerShell(smtpPort),
                escapePowerShell(smtpSsl),
                credentialsBlock
        );
    }

    private String escapePowerShell(String value) {
        return (value == null ? "" : value)
                .replace("'", "''")
                .replace("\r", "")
                .replace("\n", "`n");
    }

    private void writeOutboxCopy(String toEmail, String subject, String body) {
        writeOutboxCopy(toEmail, subject, body, false);
    }

    private void writeOutboxCopy(String toEmail, String subject, String body, boolean htmlBody) {
        String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")) + "-notification.txt";
        Path targetFile = outboxRoot.resolve(fileName);
        String output = "To: " + toEmail + System.lineSeparator()
                + "Subject: " + subject + System.lineSeparator()
                + "Content-Type: " + (htmlBody ? "text/html" : "text/plain") + System.lineSeparator()
                + System.lineSeparator()
                + body;
        try {
            Files.writeString(targetFile, output);
        } catch (IOException ignored) {
            // Fallback writing is best-effort only.
        }
    }
}
