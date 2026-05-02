package com.lulibrisync.repository;

import com.lulibrisync.model.StudentRegistrationOtpRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRegistrationOtpRequestRepository extends JpaRepository<StudentRegistrationOtpRequest, Long> {

    Optional<StudentRegistrationOtpRequest> findFirstByPendingEmailIgnoreCaseAndUsedFalseOrderByCreatedAtDesc(String pendingEmail);

    List<StudentRegistrationOtpRequest> findByPendingEmailIgnoreCaseAndUsedFalseOrderByCreatedAtDesc(String pendingEmail);
}
