package com.example.MpApp.dto.officestaff;

public class OfficeStaffProfileResponse {

    private Long id;

    private String name;

    private String email;

    private String role;

    private Integer score;

    private long assignedTasks;

    private long completedTasks;

    private long pendingTasks;

    private long rejectedTasks;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public long getAssignedTasks() {
        return assignedTasks;
    }

    public void setAssignedTasks(long assignedTasks) {
        this.assignedTasks = assignedTasks;
    }

    public long getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(long completedTasks) {
        this.completedTasks = completedTasks;
    }

    public long getPendingTasks() {
        return pendingTasks;
    }

    public void setPendingTasks(long pendingTasks) {
        this.pendingTasks = pendingTasks;
    }

    public long getRejectedTasks() {
        return rejectedTasks;
    }

    public void setRejectedTasks(long rejectedTasks) {
        this.rejectedTasks = rejectedTasks;
    }
}