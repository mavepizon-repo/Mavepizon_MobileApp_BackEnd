package com.example.MpApp.entity.teamlead;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "team_lead_permissions")
@Data
public class TeamLeadPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Assuming your Team Lead entity is called 'OfficeStaff' or a distinct 'TeamLead' entity
    // Adjust the target class type below if your entity name differs
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_lead_id", nullable = false)
    private TeamLead teamLead;

    @Column(name = "permission_date", nullable = false)
    private LocalDate permissionDate;

    @Column(name = "duration_hours")
    private Integer durationHours;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private String status; // PENDING, APPROVED, REJECTED

    @Column(name = "remarks")
    private String remarks; // Admin rejection reasons

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}