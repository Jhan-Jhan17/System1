package com.example.demo.repository;

import com.example.demo.model.ClaimRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClaimRequestRepository extends JpaRepository<ClaimRequest, Long> {
    List<ClaimRequest> findByStatus(String status);
}