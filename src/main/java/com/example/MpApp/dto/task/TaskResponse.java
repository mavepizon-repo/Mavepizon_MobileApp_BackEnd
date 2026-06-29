package com.example.MpApp.dto.task;

import com.example.MpApp.entity.enums.Priority;
import com.example.MpApp.entity.enums.TaskStatus;
import com.example.MpApp.entity.enums.TaskType;

import java.time.LocalDate;

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

    public TaskResponse(
            Long taskId,
            String title,
            String description,
            LocalDate assignedDate,
            LocalDate deadline,
            Integer progress,
            Integer estimatedHours,
            TaskStatus status,
            Priority priority,
            TaskType taskType,
            Long staffId,
            String staffName,
            String staffRole,
            String staffIdCode,
            Long teamLeadId,
            String teamLeadName,
            String teamLeadIdCode
    ) {
        this.taskId = taskId;
        this.title = title;
        this.description = description;
        this.assignedDate = assignedDate;
        this.deadline = deadline;
        this.progress = progress;
        this.estimatedHours = estimatedHours;
        this.status = status;
        this.priority = priority;
        this.taskType = taskType;
        this.staffId = staffId;
        this.staffName = staffName;
        this.staffRole = staffRole;
        this.staffIdCode = staffIdCode;
        this.teamLeadId = teamLeadId;
        this.teamLeadName = teamLeadName;
        this.teamLeadIdCode = teamLeadIdCode;
    }

    // Getters
    public Long getTaskId() { return taskId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getAssignedDate() { return assignedDate; }
    public LocalDate getDeadline() { return deadline; }
    public Integer getProgress() { return progress; }
    public Integer getEstimatedHours() { return estimatedHours; }
    public TaskStatus getStatus() { return status; }
    public Priority getPriority() { return priority; }
    public TaskType getTaskType() { return taskType; }
    public Long getStaffId() { return staffId; }
    public String getStaffName() { return staffName; }
    public String getStaffRole() { return staffRole; }
    public String getStaffIdCode() { return staffIdCode; }
    public Long getTeamLeadId() { return teamLeadId; }
    public String getTeamLeadName() { return teamLeadName; }
    public String getTeamLeadIdCode() { return teamLeadIdCode; }
}