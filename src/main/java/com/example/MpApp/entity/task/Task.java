package com.example.MpApp.entity.task;

import com.example.MpApp.entity.officestaff.OfficeStaff;
import com.example.MpApp.entity.teamlead.TeamLead;
import com.example.MpApp.entity.enums.Priority;
import com.example.MpApp.entity.enums.TaskStatus;
import com.example.MpApp.entity.enums.TaskType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "tasks")
@Data
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String title;

    private String task;

    @Column(length = 2000)
    private String description;

    private LocalDate assignedDate;

    private LocalDate deadline;

    private Integer progress = 0;

    private Integer estimatedHours;

    @Column(length = 1000)
    private String remarks;

    @Column(length = 1000)
    private String completionRemarks;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    private Priority priority;



    @Enumerated(EnumType.STRING)
    private TaskType taskType;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    private OfficeStaff staff;

    @ManyToOne
    @JoinColumn(name = "team_lead_id")
    private TeamLead teamLead;

    public Task() {
    }


}