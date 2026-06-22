package com.example.MpApp.entity.task;

import com.example.MpApp.entity.enums.TaskStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_updates")
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

    public TaskUpdate() {
    }

    public Long getId() {
        return id;
    }

    public Integer getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(Integer progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public String getWorkDoneToday() {
        return workDoneToday;
    }

    public void setWorkDoneToday(String workDoneToday) {
        this.workDoneToday = workDoneToday;
    }

    public String getBlockers() {
        return blockers;
    }

    public void setBlockers(String blockers) {
        this.blockers = blockers;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}