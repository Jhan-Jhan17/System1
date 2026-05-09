package com.example.demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class ClaimRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The student making the claim
    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    // The item they are trying to claim
    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(columnDefinition = "TEXT")
    private String ownershipDetails; // e.g., "It has a scratch on the back"

    private String status = "PENDING"; // PENDING, APPROVED, REJECTED
    
    private LocalDateTime requestDate = LocalDateTime.now();
}