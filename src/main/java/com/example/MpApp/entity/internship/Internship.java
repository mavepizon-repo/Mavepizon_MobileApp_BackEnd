package com.example.MpApp.entity.internship;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "internships")
@Data
public class Internship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "internship_code", unique = true, nullable = false)
    private String internshipCode;

    @Column(name = "internship_name", nullable = false)
    private String internshipName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String duration;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    private Double fees;

    @Column(name = "registration_fees")
    private Double registrationFees;

    @Column(name = "batch_code")
    private String batchCode;

    @Column(name = "total_seats_online")
    private Integer totalSeatsOnline;

    @Column(name = "available_seats_online")
    private Integer availableSeatsOnline;

    @Column(name = "total_seats_offline")
    private Integer totalSeatsOffline;

    @Column(name = "available_seats_offline")
    private Integer availableSeatsOffline;

    @Column(nullable = false)
    private String status; // "OPEN", "CLOSED"

    @Column(name = "zoom_link")
    private String zoomLink;

    @Column(name = "trainer_name")
    private String trainerName;

    @Column(name = "created_by")
    private String createdBy; // Filled via Backend Auth ("ADMIN" / "TEAM_LEAD")

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}