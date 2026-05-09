package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String description;
    private String reporterContact;
    private String locationFound;
    private String category;
    private String status = "AVAILABLE";
    private String imageFilename;
    private String studentId;
    private LocalDate dateLogged = LocalDate.now();

    // OOP Relationship: Many Items can be reported by One User
    @ManyToOne
    @JoinColumn(name = "reporter_id")
    private User reporter; 
}