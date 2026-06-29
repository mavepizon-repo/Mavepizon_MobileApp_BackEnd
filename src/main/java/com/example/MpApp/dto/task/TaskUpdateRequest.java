package com.example.MpApp.dto.task;

import com.example.MpApp.entity.enums.TaskStatus;
import java.time.LocalDate;

public class TaskUpdateRequest {

    private Integer progressPercentage;
    private String workDoneToday;
    private String blockers;
    private String comments;
    private String attachmentUrl;
    private TaskStatus status;

    // New fields to accept task modifications from Team Lead
    private String title;
    private String description;
    private String priority;
    private LocalDate deadline;

    public TaskUpdateRequest() {
    }

    // Getters and Setters for existing fields
    public Integer getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(Integer progressPercentage) { this.progressPercentage = progressPercentage; }
    public String getWorkDoneToday() { return workDoneToday; }
    public void setWorkDoneToday(String workDoneToday) { this.workDoneToday = workDoneToday; }
    public String getBlockers() { return blockers; }
    public void setBlockers(String blockers) { this.blockers = blockers; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    // Getters and Setters for new fields
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
}