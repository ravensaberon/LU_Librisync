package com.lulibrisync.repository;

import com.lulibrisync.model.FinePayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinePaymentRepository extends JpaRepository<FinePayment, Long> {
    List<FinePayment> findByFine_IdOrderByPaymentDateDesc(Long fineId);
}
