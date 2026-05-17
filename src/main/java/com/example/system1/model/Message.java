package com.example.system1.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String senderName;
    private String subject;
    
    @Column(columnDefinition = "TEXT")
    private String content;

    // NEW: Field for the Admin to type their reply
    @Column(columnDefinition = "TEXT")
    private String adminReply;
    
    private LocalDateTime timestamp = LocalDateTime.now();
}