package com.lulibrisync.service;

import com.lulibrisync.model.Book;
import com.lulibrisync.model.Reservation;
import com.lulibrisync.model.ReservationStatus;
import com.lulibrisync.model.Student;
import com.lulibrisync.repository.BookRepository;
import com.lulibrisync.repository.ReservationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReservationService {

    private static final List<ReservationStatus> ACTIVE_STATUSES = List.of(ReservationStatus.PENDING, ReservationStatus.READY);

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final StudentService studentService;
    private final EmailNotificationService emailNotificationService;
    private final CirculationPolicyService circulationPolicyService;
    private final int claimWindowHours;

    public ReservationService(ReservationRepository reservationRepository,
                              BookRepository bookRepository,
                              StudentService studentService,
                              EmailNotificationService emailNotificationService,
                              CirculationPolicyService circulationPolicyService,
                              @org.springframework.beans.factory.annotation.Value("${lulibrisync.reservations.claim-hours:48}") int claimWindowHours) {
        this.reservationRepository = reservationRepository;
        this.bookRepository = bookRepository;
        this.studentService = studentService;
        this.emailNotificationService = emailNotificationService;
        this.circulationPolicyService = circulationPolicyService;
        this.claimWindowHours = Math.max(1, claimWindowHours);
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAllByOrderByReservedAtDesc();
    }

    public List<Reservation> getStudentReservations(String email) {
        Student student = studentService.getStudentByEmail(email);
        return reservationRepository.findByStudent_IdOrderByReservedAtDesc(student.getId());
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
        for (Reservation reservation : reservationRepository.findByStatusInOrderByReservedAtAsc(ACTIVE_STATUSES)) {
            Long bookId = reservation.getBook().getId();
            queueSizes.put(bookId, queueSizes.getOrDefault(bookId, 0) + 1);
        }
        return queueSizes;
    }

    public Map<Long, String> getReservationStatusesForStudentBooks(String email) {
        Student student = studentService.getStudentByEmail(email);
        Map<Long, String> statuses = new LinkedHashMap<>();
        for (Reservation reservation : reservationRepository.findByStudent_IdOrderByReservedAtDesc(student.getId())) {
            if (reservation.isActive() && !statuses.containsKey(reservation.getBook().getId())) {
                statuses.put(reservation.getBook().getId(), reservation.getStatus().name());
            }
        }
        return statuses;
    }

    @Transactional
    public Reservation placeReservation(Long bookId, String email) {
        if (bookId == null) {
            throw new IllegalArgumentException("Book is required.");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found."));
        Student student = studentService.getStudentByEmail(email);
        circulationPolicyService.validateBorrowingEligibility(student);

        if (book.getAvailableQuantity() != null && book.getAvailableQuantity() > 0) {
            throw new IllegalArgumentException("This book is currently available. You can borrow it directly from the library.");
        }
        if (reservationRepository.existsByBook_IdAndStudent_IdAndStatusIn(bookId, student.getId(), ACTIVE_STATUSES)) {
            throw new IllegalArgumentException("You already have an active reservation for this book.");
        }

        Reservation reservation = new Reservation();
        reservation.setBook(book);
        reservation.setStudent(student);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setReservedAt(LocalDateTime.now());
        reservation.setQueuePosition((int) reservationRepository.countByBook_IdAndStatusIn(bookId, ACTIVE_STATUSES) + 1);

        Reservation savedReservation = reservationRepository.save(reservation);
        promoteReservationsForBook(bookId);
        return savedReservation;
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

        Reservation firstActiveReservation = reservationRepository
                .findFirstByBook_IdAndStatusInOrderByQueuePositionAscReservedAtAsc(bookId, ACTIVE_STATUSES)
                .orElse(null);

        if (firstActiveReservation == null) {
            return;
        }

        if (ReservationStatus.READY.equals(firstActiveReservation.getStatus())
                && !firstActiveReservation.getStudent().getId().equals(studentId)) {
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

        for (Reservation reservation : activeReservations) {
            if (promotableSlots < 1) {
                break;
            }
            if (ReservationStatus.PENDING.equals(reservation.getStatus())) {
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
        int index = 1;
        for (Reservation reservation : activeReservations) {
            reservation.setQueuePosition(index++);
        }
        if (!activeReservations.isEmpty()) {
            reservationRepository.saveAll(activeReservations);
        }
    }
}
