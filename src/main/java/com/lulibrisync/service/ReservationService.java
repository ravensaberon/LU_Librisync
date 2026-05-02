package com.lulibrisync.service;

import com.lulibrisync.model.Book;
import com.lulibrisync.model.Reservation;
import com.lulibrisync.model.ReservationRequestType;
import com.lulibrisync.model.ReservationStatus;
import com.lulibrisync.model.Student;
import com.lulibrisync.model.IssueStatus;
import com.lulibrisync.repository.BookRepository;
import com.lulibrisync.repository.IssueRecordRepository;
import com.lulibrisync.repository.ReservationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ReservationService {

    private static final List<ReservationStatus> ACTIVE_STATUSES = List.of(ReservationStatus.PENDING, ReservationStatus.READY);

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final IssueRecordRepository issueRecordRepository;
    private final StudentService studentService;
    private final EmailNotificationService emailNotificationService;
    private final CirculationPolicyService circulationPolicyService;
    private final int claimWindowHours;
    private final int borrowRequestWindowMinutes;
    private final int maxPreferredPickupDays;

    public ReservationService(ReservationRepository reservationRepository,
                              BookRepository bookRepository,
                              IssueRecordRepository issueRecordRepository,
                              StudentService studentService,
                              EmailNotificationService emailNotificationService,
                              CirculationPolicyService circulationPolicyService,
                              @org.springframework.beans.factory.annotation.Value("${lulibrisync.reservations.claim-hours:48}") int claimWindowHours,
                              @org.springframework.beans.factory.annotation.Value("${lulibrisync.reservations.borrow-request-minutes:60}") int borrowRequestWindowMinutes,
                              @org.springframework.beans.factory.annotation.Value("${lulibrisync.reservations.max-preferred-pickup-days:30}") int maxPreferredPickupDays) {
        this.reservationRepository = reservationRepository;
        this.bookRepository = bookRepository;
        this.issueRecordRepository = issueRecordRepository;
        this.studentService = studentService;
        this.emailNotificationService = emailNotificationService;
        this.circulationPolicyService = circulationPolicyService;
        this.claimWindowHours = Math.max(1, claimWindowHours);
        this.borrowRequestWindowMinutes = Math.max(1, borrowRequestWindowMinutes);
        this.maxPreferredPickupDays = Math.max(1, maxPreferredPickupDays);
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAllByOrderByReservedAtDesc();
    }

    public List<Reservation> getStudentReservations(String email) {
        Student student = studentService.getStudentByEmail(email);
        return reservationRepository.findByStudent_IdOrderByReservedAtDesc(student.getId());
    }

    public List<Reservation> getStudentBorrowRequests(String email) {
        Student student = studentService.getStudentByEmail(email);
        return reservationRepository.findByStudent_IdAndRequestTypeOrderByReservedAtDesc(student.getId(), ReservationRequestType.BORROW).stream()
                .filter(Reservation::isActive)
                .toList();
    }

    public List<Reservation> getStudentQueueReservations(String email) {
        Student student = studentService.getStudentByEmail(email);
        return reservationRepository.findByStudent_IdAndRequestTypeOrderByReservedAtDesc(student.getId(), ReservationRequestType.RESERVATION).stream()
                .filter(Reservation::isActive)
                .toList();
    }

    public List<Reservation> getBorrowRequests() {
        return reservationRepository.findByStatusInAndRequestTypeOrderByReservedAtAsc(ACTIVE_STATUSES, ReservationRequestType.BORROW);
    }

    public List<Reservation> getQueueReservations() {
        return reservationRepository.findByStatusInAndRequestTypeOrderByReservedAtAsc(ACTIVE_STATUSES, ReservationRequestType.RESERVATION);
    }

    public Reservation getReservationById(Long reservationId) {
        if (reservationId == null) {
            throw new IllegalArgumentException("Reservation is required.");
        }
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found."));
    }

    public long countPendingReservations() {
        return reservationRepository.findByStatusInOrderByReservedAtAsc(List.of(ReservationStatus.PENDING)).size();
    }

    public long countReadyReservations() {
        return reservationRepository.findByStatusInOrderByReservedAtAsc(List.of(ReservationStatus.READY)).size();
    }

    public Map<Long, Integer> getActiveQueueSizesByBook() {
        Map<Long, Integer> queueSizes = new LinkedHashMap<>();
        for (Reservation reservation : reservationRepository.findByStatusInAndRequestTypeOrderByReservedAtAsc(ACTIVE_STATUSES, ReservationRequestType.RESERVATION)) {
            Long bookId = reservation.getBook().getId();
            queueSizes.put(bookId, queueSizes.getOrDefault(bookId, 0) + 1);
        }
        return queueSizes;
    }

    public Map<Long, Integer> getReadyReservationCountsByBook() {
        Map<Long, Integer> readyCounts = new LinkedHashMap<>();
        for (Reservation reservation : reservationRepository.findByStatusInOrderByReservedAtAsc(List.of(ReservationStatus.READY))) {
            Long bookId = reservation.getBook().getId();
            readyCounts.put(bookId, readyCounts.getOrDefault(bookId, 0) + 1);
        }
        return readyCounts;
    }

    public Map<Long, String> getReservationStatusesForStudentBooks(String email) {
        Student student = studentService.getStudentByEmail(email);
        Map<Long, String> statuses = new LinkedHashMap<>();
        for (Reservation reservation : reservationRepository.findByStudent_IdOrderByReservedAtDesc(student.getId())) {
            if (reservation.isActive() && !statuses.containsKey(reservation.getBook().getId())) {
                statuses.put(reservation.getBook().getId(), reservation.getRequestType().name() + ":" + reservation.getStatus().name());
            }
        }
        return statuses;
    }

    public int getMaxPreferredPickupDays() {
        return maxPreferredPickupDays;
    }

    public int getBorrowRequestWindowMinutes() {
        return borrowRequestWindowMinutes;
    }

    @Transactional
    public Reservation placeBorrowRequest(Long bookId, String email) {
        if (bookId == null) {
            throw new IllegalArgumentException("Book is required.");
        }

        promoteReservationsForBook(bookId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found."));
        Student student = studentService.getStudentByEmail(email);
        circulationPolicyService.validateBorrowingEligibility(student);

        if (issueRecordRepository.existsByBook_IdAndStudent_IdAndStatusIn(bookId, student.getId(), List.of(IssueStatus.ISSUED, IssueStatus.OVERDUE))) {
            throw new IllegalArgumentException("You already have this book on loan.");
        }
        if (reservationRepository.existsByBook_IdAndStudent_IdAndStatusIn(bookId, student.getId(), ACTIVE_STATUSES)) {
            throw new IllegalArgumentException("You already have an active pickup request or reservation for this book.");
        }
        if (getWalkInBorrowableCopyCount(book) < 1) {
            throw new IllegalArgumentException("No walk-in copy is available right now. Please join the reservation queue instead.");
        }

        Reservation reservation = new Reservation();
        reservation.setBook(book);
        reservation.setStudent(student);
        reservation.setRequestType(ReservationRequestType.BORROW);
        reservation.setStatus(ReservationStatus.READY);
        reservation.setReservedAt(LocalDateTime.now());
        reservation.setPreferredPickupDate(null);
        reservation.setQueuePosition(0);
        reservation.setExpiresAt(LocalDateTime.now().plusMinutes(borrowRequestWindowMinutes));

        Reservation savedReservation = reservationRepository.save(reservation);
        emailNotificationService.queueReservationReadyNotification(savedReservation);
        promoteReservationsForBook(bookId);
        return getReservationById(savedReservation.getId());
    }

    @Transactional
    public Reservation placeReservation(Long bookId, String email) {
        return placeReservation(bookId, email, LocalDate.now());
    }

    @Transactional
    public Reservation placeReservation(Long bookId, String email, LocalDate preferredPickupDate) {
        if (bookId == null) {
            throw new IllegalArgumentException("Book is required.");
        }

        promoteReservationsForBook(bookId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found."));
        Student student = studentService.getStudentByEmail(email);
        circulationPolicyService.validateBorrowingEligibility(student);
        if (issueRecordRepository.existsByBook_IdAndStudent_IdAndStatusIn(bookId, student.getId(), List.of(IssueStatus.ISSUED, IssueStatus.OVERDUE))) {
            throw new IllegalArgumentException("You already have this book on loan.");
        }
        if (reservationRepository.existsByBook_IdAndStudent_IdAndStatusIn(bookId, student.getId(), ACTIVE_STATUSES)) {
            throw new IllegalArgumentException("You already have an active reservation for this book.");
        }

        LocalDate requestedPickupDate = preferredPickupDate == null ? LocalDate.now() : preferredPickupDate;
        if (requestedPickupDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Preferred pickup date cannot be earlier than today.");
        }
        if (requestedPickupDate.isAfter(LocalDate.now().plusDays(maxPreferredPickupDays))) {
            throw new IllegalArgumentException("Preferred pickup date is too far ahead. Please choose a nearer date.");
        }

        Reservation reservation = new Reservation();
        reservation.setBook(book);
        reservation.setStudent(student);
        reservation.setRequestType(ReservationRequestType.RESERVATION);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setReservedAt(LocalDateTime.now());
        reservation.setPreferredPickupDate(requestedPickupDate);
        reservation.setQueuePosition((int) reservationRepository.countByBook_IdAndStatusInAndRequestType(bookId, ACTIVE_STATUSES, ReservationRequestType.RESERVATION) + 1);

        Reservation savedReservation = reservationRepository.save(reservation);
        reindexQueue(bookId);
        promoteReservationsForBook(bookId);
        return getReservationById(savedReservation.getId());
    }

    @Transactional
    public void cancelReservationByStudent(Long reservationId, String email) {
        Student student = studentService.getStudentByEmail(email);
        Reservation reservation = getReservationById(reservationId);
        if (!reservation.getStudent().getId().equals(student.getId())) {
            throw new IllegalArgumentException("You can only cancel your own reservation.");
        }
        cancelReservation(reservation);
    }

    @Transactional
    public void cancelReservationByAdmin(Long reservationId) {
        cancelReservation(getReservationById(reservationId));
    }

    @Transactional
    public void beforeIssueValidation(Long bookId, Long studentId) {
        promoteReservationsForBook(bookId);

        List<Reservation> activeReservations = reservationRepository.findByBook_IdAndStatusInOrderByQueuePositionAscReservedAtAsc(bookId, ACTIVE_STATUSES);
        if (activeReservations.isEmpty()) {
            return;
        }

        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) {
            return;
        }

        boolean studentHasReadyReservation = activeReservations.stream()
                .anyMatch(reservation -> ReservationStatus.READY.equals(reservation.getStatus())
                        && reservation.getStudent().getId().equals(studentId));
        if (studentHasReadyReservation) {
            return;
        }

        int freeWalkInCopies = Math.max(0, getAvailableCopyCount(book) - (int) activeReservations.stream()
                .filter(reservation -> ReservationStatus.READY.equals(reservation.getStatus()))
                .count());
        if (freeWalkInCopies > 0) {
            return;
        }

        Reservation firstReadyReservation = activeReservations.stream()
                .filter(reservation -> ReservationStatus.READY.equals(reservation.getStatus()))
                .findFirst()
                .orElse(null);
        if (firstReadyReservation != null && !firstReadyReservation.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("This copy is reserved for the next student in the queue.");
        }
    }

    @Transactional
    public void markReservationClaimed(Long bookId, Long studentId) {
        List<Reservation> activeReservations = reservationRepository.findByBook_IdAndStatusInOrderByQueuePositionAscReservedAtAsc(bookId, ACTIVE_STATUSES);
        for (Reservation reservation : activeReservations) {
            if (reservation.getStudent().getId().equals(studentId)) {
                emailNotificationService.cancelReservationReadyNotification(reservation);
                reservation.setStatus(ReservationStatus.CLAIMED);
                reservation.setExpiresAt(null);
                reservationRepository.save(reservation);
                reindexQueue(bookId);
                return;
            }
        }
    }

    @Transactional
    public void promoteReservationsForBook(Long bookId) {
        if (bookId == null) {
            return;
        }

        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) {
            return;
        }

        List<Reservation> activeReservations = reservationRepository.findByBook_IdAndStatusInOrderByQueuePositionAscReservedAtAsc(bookId, ACTIVE_STATUSES);
        List<Reservation> queueReservations = activeReservations.stream()
                .filter(Reservation::isQueueReservation)
                .toList();
        if (activeReservations.isEmpty()) {
            return;
        }

        int availableCopies = book.getAvailableQuantity() == null ? 0 : book.getAvailableQuantity();
        long readyCount = activeReservations.stream()
                .filter(reservation -> ReservationStatus.READY.equals(reservation.getStatus()))
                .count();
        int promotableSlots = Math.max(0, availableCopies - (int) readyCount);
        if (promotableSlots < 1) {
            reindexQueue(bookId);
            return;
        }

        for (Reservation reservation : queueReservations) {
            if (promotableSlots < 1) {
                break;
            }
            if (ReservationStatus.PENDING.equals(reservation.getStatus()) && isReadyForPickup(reservation)) {
                reservation.setStatus(ReservationStatus.READY);
                reservation.setExpiresAt(LocalDateTime.now().plusHours(claimWindowHours));
                reservationRepository.save(reservation);
                emailNotificationService.queueReservationReadyNotification(reservation);
                promotableSlots--;
            }
        }

        reindexQueue(bookId);
    }

    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void expireReadyReservations() {
        List<Reservation> expiredReservations = reservationRepository.findByStatusAndExpiresAtBefore(ReservationStatus.READY, LocalDateTime.now());
        for (Reservation reservation : expiredReservations) {
            emailNotificationService.cancelReservationReadyNotification(reservation);
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservation.setExpiresAt(null);
            reservationRepository.save(reservation);
            reindexQueue(reservation.getBook().getId());
            promoteReservationsForBook(reservation.getBook().getId());
        }
    }

    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void syncReadyReservations() {
        Set<Long> processedBookIds = new LinkedHashSet<>();
        for (Reservation reservation : reservationRepository.findByStatusInOrderByReservedAtAsc(ACTIVE_STATUSES)) {
            if (processedBookIds.add(reservation.getBook().getId())) {
                promoteReservationsForBook(reservation.getBook().getId());
            }
        }
    }

    private void cancelReservation(Reservation reservation) {
        if (!reservation.isActive()) {
            throw new IllegalArgumentException("Only active reservations can be cancelled.");
        }

        emailNotificationService.cancelReservationReadyNotification(reservation);
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setExpiresAt(null);
        reservationRepository.save(reservation);
        reindexQueue(reservation.getBook().getId());
        promoteReservationsForBook(reservation.getBook().getId());
    }

    private void reindexQueue(Long bookId) {
        List<Reservation> activeReservations = reservationRepository.findByBook_IdAndStatusInOrderByQueuePositionAscReservedAtAsc(bookId, ACTIVE_STATUSES);
        List<Reservation> queueReservations = activeReservations.stream()
                .filter(Reservation::isQueueReservation)
                .sorted(Comparator
                .comparing(this::resolvePriorityDate)
                .thenComparing(Reservation::getReservedAt)
                .thenComparing(Reservation::getId))
                .toList();
        int index = 1;
        for (Reservation reservation : queueReservations) {
            reservation.setQueuePosition(index++);
        }
        if (!queueReservations.isEmpty()) {
            reservationRepository.saveAll(queueReservations);
        }
    }

    private int getWalkInBorrowableCopyCount(Book book) {
        if (book == null || book.getId() == null) {
            return 0;
        }
        int readyReservationCount = (int) reservationRepository.countByBook_IdAndStatusIn(book.getId(), List.of(ReservationStatus.READY));
        return Math.max(0, getAvailableCopyCount(book) - readyReservationCount);
    }

    private int getAvailableCopyCount(Book book) {
        return book == null || book.getAvailableQuantity() == null ? 0 : Math.max(0, book.getAvailableQuantity());
    }

    private boolean isReadyForPickup(Reservation reservation) {
        LocalDate preferredPickupDate = reservation.getPreferredPickupDate();
        return preferredPickupDate == null || !preferredPickupDate.isAfter(LocalDate.now());
    }

    private LocalDate resolvePriorityDate(Reservation reservation) {
        if (reservation.getPreferredPickupDate() != null) {
            return reservation.getPreferredPickupDate();
        }
        return reservation.getReservedAt() == null ? LocalDate.now() : reservation.getReservedAt().toLocalDate();
    }
}
