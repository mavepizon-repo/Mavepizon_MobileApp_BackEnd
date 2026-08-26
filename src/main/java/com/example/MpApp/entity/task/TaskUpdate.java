package com.example.MpApp.entity.task;

import com.example.MpApp.entity.enums.TaskStatus;
import com.example.MpApp.entity.officestaff.OfficeStaff;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_updates")
@Data
public class TaskUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer progressPercentage;

    @Column(length = 2000)
    private String workDoneToday;

    @Column(length = 1000)
    private String blockers;

    @Column(length = 1000)
    private String comments;

    private String attachmentUrl;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne
    @JoinColumn(name = "office_staff_id")
    private OfficeStaff updatedBy;


}