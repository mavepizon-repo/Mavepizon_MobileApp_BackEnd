package com.example.MpApp.dto.task;

import com.example.MpApp.entity.enums.Priority;
import com.example.MpApp.entity.enums.TaskStatus;
import com.example.MpApp.entity.enums.TaskType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskResponse {

    private Long taskId;
    private String title;
    private String description;
    private LocalDate assignedDate;
    private LocalDate deadline;
    private Integer progress;
    private Integer estimatedHours;
    private TaskStatus status;
    private Priority priority;
    private TaskType taskType;

    private Long staffId;
    private String staffName;
    private String staffRole;
    private String staffIdCode; // Represents the alphanumeric ID (e.g., MPCBTDE001)

    private Long teamLeadId;
    private String teamLeadName;
    private String teamLeadIdCode; // Represents the alphanumeric TL ID



    // Getters

}