package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String studentId;
    
    private String fullName;
    
    @Column(unique = true)
    private String email;
    
    private String password;
    
    private String role = "STUDENT";
}