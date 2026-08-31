package com.example.MpApp.entity.officestaff;

import com.example.MpApp.entity.teamlead.TeamLead;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "office_staff_permissions")
@Getter
@Setter
public class OfficeStaffPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private OfficeStaff staff;

    @Column(nullable = false)
    private LocalDate permissionDate;

    @Column(nullable = false)
    private int durationHours; // Supports 1 or 2 hours

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private String status; // PENDING, APPROVED, REJECTED

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_lead_id")
    private TeamLead teamLead;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}