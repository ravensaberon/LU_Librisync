package com.lulibrisync.repository;

import com.lulibrisync.model.Fine;
import com.lulibrisync.model.FineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface FineRepository extends JpaRepository<Fine, Long> {

    Optional<Fine> findByIssueRecord_Id(Long issueRecordId);

    List<Fine> findAllByOrderByCalculatedAtDesc();

    List<Fine> findTop20ByOrderByCalculatedAtDesc();

    List<Fine> findByStudent_IdOrderByCalculatedAtDesc(Long studentId);

    List<Fine> findTop12ByStatusOrderByCalculatedAtDesc(FineStatus status);

    long countByStatus(FineStatus status);

    long countByStudent_IdAndStatus(Long studentId, FineStatus status);

    @Query("""
            select coalesce(sum(f.amount - f.paidAmount), 0)
            from Fine f
            where f.status in (:statuses)
            """)
    BigDecimal sumOutstandingAmount(@Param("statuses") List<FineStatus> statuses);

    @Query("""
            select coalesce(sum(f.amount - f.paidAmount), 0)
            from Fine f
            where f.student.id = :studentId
              and f.status in (:statuses)
            """)
    BigDecimal sumOutstandingAmountByStudentId(@Param("studentId") Long studentId,
                                               @Param("statuses") List<FineStatus> statuses);

    @Query("""
            select coalesce(sum(f.amount), 0)
            from Fine f
            where f.status = :status
            """)
    BigDecimal sumTotalAmountByStatus(@Param("status") FineStatus status);
}
