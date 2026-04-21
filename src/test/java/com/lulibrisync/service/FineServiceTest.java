package com.lulibrisync.service;

import com.lulibrisync.model.Fine;
import com.lulibrisync.model.FinePayment;
import com.lulibrisync.model.FineStatus;
import com.lulibrisync.repository.FinePaymentRepository;
import com.lulibrisync.repository.FineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FineServiceTest {

    @Mock
    private FineRepository fineRepository;

    @Mock
    private FinePaymentRepository finePaymentRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private FineService fineService;

    private Fine fine;

    @BeforeEach
    void setUp() {
        fine = new Fine();
        fine.setId(1L);
        fine.setAmount(new BigDecimal("100.00"));
        fine.setPaidAmount(BigDecimal.ZERO);
        fine.setStatus(FineStatus.UNPAID);
    }

    @Test
    void recordPartialPayment_ShouldUpdateStatusToPartiallyPaid() {
        when(fineRepository.findById(1L)).thenReturn(Optional.of(fine));
        when(fineRepository.save(any(Fine.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Fine result = fineService.recordPayment(1L, new BigDecimal("40.00"), "CASH", "REC-001", "admin@test.com", "Partial");

        assertEquals(FineStatus.PARTIALLY_PAID, result.getStatus());
        assertTrue(new BigDecimal("40.00").compareTo(result.getPaidAmount()) == 0);
        assertTrue(new BigDecimal("60.00").compareTo(result.getRemainingAmount()) == 0);

        verify(finePaymentRepository).save(any(FinePayment.class));
        verify(auditLogService).log(eq("admin@test.com"), eq("FINE_PAYMENT"), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void recordFullPayment_ShouldUpdateStatusToPaid() {
        when(fineRepository.findById(1L)).thenReturn(Optional.of(fine));
        when(fineRepository.save(any(Fine.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Fine result = fineService.recordPayment(1L, new BigDecimal("100.00"), "CASH", "REC-002", "admin@test.com", "Full");

        assertEquals(FineStatus.PAID, result.getStatus());
        assertTrue(new BigDecimal("100.00").compareTo(result.getPaidAmount()) == 0);
        assertTrue(BigDecimal.ZERO.compareTo(result.getRemainingAmount()) == 0);
        assertNotNull(result.getPaidAt());
    }

    @Test
    void recordPayment_ExceedingAmount_ShouldThrowException() {
        when(fineRepository.findById(1L)).thenReturn(Optional.of(fine));

        assertThrows(IllegalArgumentException.class, () ->
            fineService.recordPayment(1L, new BigDecimal("110.00"), "CASH", "REC-003", "admin@test.com", "Invalid")
        );
    }
}
