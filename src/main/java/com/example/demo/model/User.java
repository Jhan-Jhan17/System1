package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users") // "user" is a reserved word in SQL, so we name the table "users"
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String studentId; // e.g., 21-60123
    
    private String fullName;
    private String password;
    
    private String role; // "STUDENT" or "ADMIN"
}