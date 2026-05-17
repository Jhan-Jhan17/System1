package com.example.system1.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ClaimRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id") 
    private User student;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    private String studentId;
    private String claimToken;
    private String proofImage; // Stores the filename of the uploaded image

    @Column(columnDefinition = "TEXT")
    private String ownershipDetails;

    private String status = "PENDING";
    private LocalDateTime requestDate = LocalDateTime.now();
}