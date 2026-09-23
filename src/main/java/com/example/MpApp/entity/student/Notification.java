package com.example.MpApp.entity.student;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Student student;

    private String message;
    private LocalDateTime createdAt;
    private boolean isRead = false;
    // ... Getters/Setters
}
