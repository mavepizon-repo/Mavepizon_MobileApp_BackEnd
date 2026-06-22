package com.example.MpApp.entity.teamlead;

import com.example.MpApp.entity.admin.Admin;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class TeamLeadLeave {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private TeamLead teamLead; // The requester

    @ManyToOne
    private Admin approvedBy;   // The Admin who reviews this

    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String status;      // PENDING, APPROVED, REJECTED
}
