package com.lulibrisync.repository;

import com.lulibrisync.model.StudentPasswordChangeOtpRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentPasswordChangeOtpRequestRepository extends JpaRepository<StudentPasswordChangeOtpRequest, Long> {

    Optional<StudentPasswordChangeOtpRequest> findFirstByStudent_IdAndUsedFalseOrderByCreatedAtDesc(Long studentId);

    List<StudentPasswordChangeOtpRequest> findByStudent_IdAndUsedFalseOrderByCreatedAtDesc(Long studentId);
}
