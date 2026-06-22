package com.example.MpApp.entity.officestaff;

import com.example.MpApp.entity.teamlead.TeamLead;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class OfficeStaffLeave {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private OfficeStaff staff;

    @ManyToOne
    private TeamLead approvedBy; // TeamLead approves staff leave

    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String status; // PENDING, APPROVED, REJECTED
}
