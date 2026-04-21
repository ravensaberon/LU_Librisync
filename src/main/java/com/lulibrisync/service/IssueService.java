package com.lulibrisync.service;

import com.lulibrisync.dto.AdminDashboardChartPoint;
import com.lulibrisync.model.Book;
import com.lulibrisync.model.IssueRecord;
import com.lulibrisync.model.IssueStatus;
import com.lulibrisync.model.Student;
import com.lulibrisync.model.User;
import com.lulibrisync.model.UserStatus;
import com.lulibrisync.repository.BookRepository;
import com.lulibrisync.repository.IssueRecordRepository;
import com.lulibrisync.repository.StudentRepository;
import com.lulibrisync.repository.UserRepository;
import com.lulibrisync.repository.projection.BookBorrowStat;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class IssueService {

    private final IssueRecordRepository issueRecordRepository;
    private final BookRepository bookRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final StudentService studentService;
    private final ReservationService reservationService;
    private final EmailNotificationService emailNotificationService;
    private final FineService fineService;
    private final CirculationPolicyService circulationPolicyService;
    private final BigDecimal dailyFine;

    public IssueService(IssueRecordRepository issueRecordRepository,
                        BookRepository bookRepository,
                        StudentRepository studentRepository,
                        UserRepository userRepository,
                        StudentService studentService,
                        ReservationService reservationService,
                        EmailNotificationService emailNotificationService,
                        FineService fineService,
                        CirculationPolicyService circulationPolicyService,
                        @org.springframework.beans.factory.annotation.Value("${lulibrisync.circulation.daily-fine:10.00}") BigDecimal dailyFine) {
        this.issueRecordRepository = issueRecordRepository;
        this.bookRepository = bookRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.studentService = studentService;
        this.reservationService = reservationService;
        this.emailNotificationService = emailNotificationService;
        this.fineService = fineService;
        this.circulationPolicyService = circulationPolicyService;
        this.dailyFine = dailyFine == null ? new BigDecimal("10.00") : dailyFine;
    }

    @Transactional
    public IssueRecord issueBook(Long bookId, Long studentId, LocalDate dueDate, String issuerEmail, String remarks) {
        if (bookId == null) {
            throw new IllegalArgumentException("Book is required.");
        }
        if (studentId == null) {
            throw new IllegalArgumentException("Student is required.");
        }
        if (dueDate == null) {
            throw new IllegalArgumentException("Due date is required.");
        }
        if (dueDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Due date cannot be earlier than today.");
        }
        if (dueDate.isAfter(LocalDate.now().plusDays(circulationPolicyService.getMaxLoanDays()))) {
            throw new IllegalArgumentException("Due date exceeds the maximum loan period allowed by current circulation policy.");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found."));
        if (book.getAvailableQuantity() == null || book.getAvailableQuantity() < 1) {
            throw new IllegalArgumentException("Selected book is not available.");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found."));
        if (!UserStatus.ACTIVE.equals(student.getUser().getStatus())) {
            throw new IllegalArgumentException("This student account is inactive and cannot borrow items.");
        }
        if (issueRecordRepository.existsByBook_IdAndStudent_IdAndStatusIn(bookId, studentId, List.of(IssueStatus.ISSUED, IssueStatus.OVERDUE))) {
            throw new IllegalArgumentException("This student already has an active loan for the selected book.");
        }
        circulationPolicyService.validateBorrowingEligibility(student);
        User issuedBy = userRepository.findByEmailIgnoreCase(issuerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Issuing user not found."));
        reservationService.beforeIssueValidation(bookId, studentId);

        IssueRecord issueRecord = new IssueRecord();
        issueRecord.setBook(book);
        issueRecord.setStudent(student);
        issueRecord.setIssuedBy(issuedBy);
        issueRecord.setIssueDate(LocalDateTime.now());
        issueRecord.setDueDate(dueDate.atTime(17, 0));
        issueRecord.setStatus(IssueStatus.ISSUED);
        issueRecord.setFineAmount(BigDecimal.ZERO);
        issueRecord.setRemarks(blankToNull(remarks));
        issueRecord.setQrIssueCode(buildIssueCode(book, student));

        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        bookRepository.save(book);
        IssueRecord savedIssueRecord = issueRecordRepository.save(issueRecord);
        reservationService.markReservationClaimed(bookId, studentId);
        emailNotificationService.queueDueReminder(savedIssueRecord);
        fineService.syncFineForIssue(savedIssueRecord);
        return savedIssueRecord;
    }

    @Transactional
    public IssueRecord returnBook(Long issueRecordId) {
        IssueRecord issueRecord = issueRecordRepository.findById(issueRecordId)
                .orElseThrow(() -> new IllegalArgumentException("Issue record not found."));

        if (issueRecord.isReturned()) {
            throw new IllegalArgumentException("Book is already returned.");
        }

        LocalDateTime returnTimestamp = LocalDateTime.now();
        issueRecord.setReturnDate(returnTimestamp);
        issueRecord.setFineAmount(calculateFine(issueRecord.getDueDate(), returnTimestamp));
        issueRecord.setStatus(IssueStatus.RETURNED);

        Book book = issueRecord.getBook();
        int currentAvailable = book.getAvailableQuantity() == null ? 0 : book.getAvailableQuantity();
        int totalQuantity = book.getQuantity() == null ? 1 : book.getQuantity();
        book.setAvailableQuantity(Math.min(totalQuantity, currentAvailable + 1));
        bookRepository.save(book);
        IssueRecord savedIssueRecord = issueRecordRepository.save(issueRecord);
        emailNotificationService.cancelDueReminder(savedIssueRecord);
        reservationService.promoteReservationsForBook(book.getId());
        fineService.syncFineForIssue(savedIssueRecord);
        return savedIssueRecord;
    }

    @Transactional
    public void refreshOverdueStatuses() {
        List<IssueRecord> activeIssues = issueRecordRepository.findByStatusInOrderByIssueDateDesc(List.of(IssueStatus.ISSUED, IssueStatus.OVERDUE));
        LocalDateTime now = LocalDateTime.now();

        for (IssueRecord issueRecord : activeIssues) {
            if (issueRecord.getReturnDate() != null) {
                continue;
            }

            BigDecimal fine = calculateFine(issueRecord.getDueDate(), now);
            if (now.isAfter(issueRecord.getDueDate())) {
                issueRecord.setStatus(IssueStatus.OVERDUE);
                issueRecord.setFineAmount(fine);
            } else {
                issueRecord.setStatus(IssueStatus.ISSUED);
                issueRecord.setFineAmount(BigDecimal.ZERO);
            }
        }

        List<IssueRecord> savedIssues = issueRecordRepository.saveAll(activeIssues);
        for (IssueRecord savedIssue : savedIssues) {
            fineService.syncFineForIssue(savedIssue);
        }
    }

    public List<IssueRecord> getActiveIssues() {
        refreshOverdueStatuses();
        return issueRecordRepository.findActiveIssuesOrdered(List.of(IssueStatus.ISSUED, IssueStatus.OVERDUE));
    }

    public List<IssueRecord> getRecentIssues() {
        refreshOverdueStatuses();
        return issueRecordRepository.findTop8ByOrderByIssueDateDesc();
    }

    public List<IssueRecord> getAllIssues() {
        refreshOverdueStatuses();
        return issueRecordRepository.findAllByOrderByIssueDateDesc();
    }

    public List<IssueRecord> getStudentIssues(String email) {
        refreshOverdueStatuses();
        Student student = studentService.getStudentByEmail(email);
        return issueRecordRepository.findByStudent_IdOrderByIssueDateDesc(student.getId());
    }

    public List<IssueRecord> getStudentIssuesByStudentId(String studentId) {
        refreshOverdueStatuses();
        Student student = studentService.getStudentByStudentId(studentId);
        return issueRecordRepository.findByStudent_IdOrderByIssueDateDesc(student.getId());
    }

    public Map<Long, String> getActiveIssueStatusesForStudentBooks(String email) {
        Map<Long, String> activeIssueStatuses = new LinkedHashMap<>();
        for (IssueRecord issueRecord : getStudentIssues(email)) {
            if (!issueRecord.isReturned() && !activeIssueStatuses.containsKey(issueRecord.getBook().getId())) {
                activeIssueStatuses.put(issueRecord.getBook().getId(), issueRecord.getStatus().name());
            }
        }
        return activeIssueStatuses;
    }

    public Map<Long, Long> getActiveIssueIdsForStudentBooks(String email) {
        Map<Long, Long> activeIssueIds = new LinkedHashMap<>();
        for (IssueRecord issueRecord : getStudentIssues(email)) {
            if (!issueRecord.isReturned() && !activeIssueIds.containsKey(issueRecord.getBook().getId())) {
                activeIssueIds.put(issueRecord.getBook().getId(), issueRecord.getId());
            }
        }
        return activeIssueIds;
    }

    public Map<Long, LocalDate> getActiveIssueDueDatesForStudentBooks(String email) {
        Map<Long, LocalDate> activeIssueDueDates = new LinkedHashMap<>();
        for (IssueRecord issueRecord : getStudentIssues(email)) {
            if (!issueRecord.isReturned() && !activeIssueDueDates.containsKey(issueRecord.getBook().getId()) && issueRecord.getDueDate() != null) {
                activeIssueDueDates.put(issueRecord.getBook().getId(), issueRecord.getDueDate().toLocalDate());
            }
        }
        return activeIssueDueDates;
    }

    public Map<Long, LocalDate> getNextAvailableDatesByBookIds(List<Long> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, LocalDate> nextAvailableDates = new LinkedHashMap<>();
        for (IssueRecord issueRecord : issueRecordRepository.findByBook_IdInAndStatusInOrderByDueDateAsc(
                bookIds,
                List.of(IssueStatus.ISSUED, IssueStatus.OVERDUE))) {
            Long bookId = issueRecord.getBook().getId();
            if (!nextAvailableDates.containsKey(bookId) && issueRecord.getDueDate() != null) {
                nextAvailableDates.put(bookId, issueRecord.getDueDate().toLocalDate());
            }
        }
        return nextAvailableDates;
    }

    public List<BookBorrowStat> getMostBorrowedBooks() {
        return issueRecordRepository.findMostBorrowedBooks(PageRequest.of(0, 5));
    }

    public long countTransactionsManagedBy(String adminEmail) {
        return issueRecordRepository.countByIssuedBy_EmailIgnoreCase(adminEmail);
    }

    public IssueRecord getIssueById(Long issueId) {
        if (issueId == null) {
            throw new IllegalArgumentException("Issue record is required.");
        }
        return issueRecordRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("Issue record not found."));
    }

    @Transactional
    public IssueRecord returnBookByStudent(Long issueId, String email) {
        Student student = studentService.getStudentByEmail(email);
        IssueRecord issueRecord = getIssueById(issueId);
        if (!issueRecord.getStudent().getId().equals(student.getId())) {
            throw new IllegalArgumentException("You can only return books issued to your account.");
        }
        return returnBook(issueId);
    }

    @Transactional
    public IssueRecord updateIssue(Long issueId,
                                   LocalDate dueDate,
                                   String remarks) {
        IssueRecord issueRecord = getIssueById(issueId);
        if (dueDate == null) {
            throw new IllegalArgumentException("Due date is required.");
        }

        issueRecord.setDueDate(dueDate.atTime(17, 0));
        issueRecord.setRemarks(blankToNull(remarks));

        if (!issueRecord.isReturned()) {
            LocalDateTime now = LocalDateTime.now();
            BigDecimal fine = calculateFine(issueRecord.getDueDate(), now);
            if (now.isAfter(issueRecord.getDueDate())) {
                issueRecord.setStatus(IssueStatus.OVERDUE);
                issueRecord.setFineAmount(fine);
            } else {
                issueRecord.setStatus(IssueStatus.ISSUED);
                issueRecord.setFineAmount(BigDecimal.ZERO);
            }
        }

        IssueRecord savedIssueRecord = issueRecordRepository.save(issueRecord);
        if (!savedIssueRecord.isReturned()) {
            emailNotificationService.queueDueReminder(savedIssueRecord);
        }
        fineService.syncFineForIssue(savedIssueRecord);
        return savedIssueRecord;
    }

    @Transactional
    public void deleteIssue(Long issueId) {
        IssueRecord issueRecord = getIssueById(issueId);
        emailNotificationService.cancelDueReminder(issueRecord);
        fineService.removeFineForIssue(issueId);
        if (!issueRecord.isReturned()) {
            Book book = issueRecord.getBook();
            int currentAvailable = book.getAvailableQuantity() == null ? 0 : book.getAvailableQuantity();
            int totalQuantity = book.getQuantity() == null ? 1 : book.getQuantity();
            book.setAvailableQuantity(Math.min(totalQuantity, currentAvailable + 1));
            bookRepository.save(book);
            reservationService.promoteReservationsForBook(book.getId());
        }
        issueRecordRepository.delete(issueRecord);
    }

    public List<AdminDashboardChartPoint> getWeeklyCirculationChart() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);
        Map<LocalDate, long[]> chartData = new LinkedHashMap<>();

        for (LocalDate date = startDate; !date.isAfter(today); date = date.plusDays(1)) {
            chartData.put(date, new long[]{0L, 0L});
        }

        for (IssueRecord issueRecord : issueRecordRepository.findAll()) {
            if (issueRecord.getIssueDate() != null) {
                LocalDate issueDate = issueRecord.getIssueDate().toLocalDate();
                if (!issueDate.isBefore(startDate) && !issueDate.isAfter(today)) {
                    chartData.get(issueDate)[0]++;
                }
            }

            if (issueRecord.getReturnDate() != null) {
                LocalDate returnDate = issueRecord.getReturnDate().toLocalDate();
                if (!returnDate.isBefore(startDate) && !returnDate.isAfter(today)) {
                    chartData.get(returnDate)[1]++;
                }
            }
        }

        long maxValue = 1;
        for (long[] values : chartData.values()) {
            maxValue = Math.max(maxValue, Math.max(values[0], values[1]));
        }

        final long chartMaxValue = maxValue;
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);
        return chartData.entrySet().stream()
                .map(entry -> new AdminDashboardChartPoint(
                        labelFormatter.format(entry.getKey()),
                        entry.getValue()[0],
                        entry.getValue()[1],
                        heightPercentage(entry.getValue()[0], chartMaxValue),
                        heightPercentage(entry.getValue()[1], chartMaxValue)
                ))
                .toList();
    }

    private BigDecimal calculateFine(LocalDateTime dueDate, LocalDateTime referenceDate) {
        if (dueDate == null || referenceDate == null || !referenceDate.isAfter(dueDate)) {
            return BigDecimal.ZERO;
        }

        long overdueDays = ChronoUnit.DAYS.between(dueDate.toLocalDate(), referenceDate.toLocalDate());
        if (overdueDays < 1) {
            overdueDays = 1;
        }
        return dailyFine.multiply(BigDecimal.valueOf(overdueDays));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String buildIssueCode(Book book, Student student) {
        String studentCode = student.getStudentId() == null ? "STUDENT" : student.getStudentId().replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ENGLISH);
        String issueDateCode = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ENGLISH));
        return "LU-ISSUE-" + book.getId() + "-" + studentCode + "-" + issueDateCode;
    }

    private int heightPercentage(long value, long maxValue) {
        if (value < 1) {
            return 0;
        }
        return Math.max(18, (int) Math.round((value * 100.0) / maxValue));
    }
}
