package com.example.MpApp.dto.task;

import com.example.MpApp.entity.enums.TaskStatus;

public class TaskUpdateRequest {

    private Integer progressPercentage;

    private String workDoneToday;

    private String blockers;

    private String comments;

    private String attachmentUrl;

    private TaskStatus status;

    public TaskUpdateRequest() {
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
}