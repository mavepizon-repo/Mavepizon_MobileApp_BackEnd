package com.example.MpApp.dto.task;

import com.example.MpApp.entity.enums.Priority;
import com.example.MpApp.entity.enums.TaskType;

import java.time.LocalDate;

public class TaskRequest {

    private Long staffId;

    private String title;

    private String description;

    private LocalDate deadline;

    private TaskType taskType;

    private Priority priority;

    private Integer estimatedHours;

    private String remarks;

    public TaskRequest() {
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Integer getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(Integer estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}