package com.example.system1.repository;

import com.example.system1.model.ClaimRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClaimRequestRepository extends JpaRepository<ClaimRequest, Long> {
    List<ClaimRequest> findByStatus(String status);
    List<ClaimRequest> findByStudentId(String studentId); // NEW: Search by student
}