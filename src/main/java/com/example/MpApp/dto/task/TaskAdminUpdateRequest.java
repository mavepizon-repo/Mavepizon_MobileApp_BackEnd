package com.example.MpApp.dto.task;

import com.example.MpApp.entity.enums.Priority;
import com.example.MpApp.entity.enums.TaskStatus;
import com.example.MpApp.entity.enums.TaskType;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class TaskAdminUpdateRequest {

    // Getters and Setters
    private String title;
    private String description;
    private Priority priority;
    private LocalDate deadline;
    private TaskStatus status;
    private Integer estimatedHours;
    private String remarks;
    private String completionRemarks;
    private TaskType taskType;

    public TaskAdminUpdateRequest() {
    }

    public void setTitle(String title) { this.title = title; }

    public void setDescription(String description) { this.description = description; }

    public void setPriority(Priority priority) { this.priority = priority; }

    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public void setStatus(TaskStatus status) { this.status = status; }

    public void setEstimatedHours(Integer estimatedHours) { this.estimatedHours = estimatedHours; }

    public void setRemarks(String remarks) { this.remarks = remarks; }

    public void setCompletionRemarks(String completionRemarks) { this.completionRemarks = completionRemarks; }

    public void setTaskType(TaskType taskType) { this.taskType = taskType; }
}