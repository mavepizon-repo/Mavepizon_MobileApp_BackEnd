package com.example.MpApp.entity.task;

import com.example.MpApp.entity.officestaff.OfficeStaff;
import com.example.MpApp.entity.teamlead.TeamLead;
import com.example.MpApp.entity.enums.Priority;
import com.example.MpApp.entity.enums.TaskStatus;
import com.example.MpApp.entity.enums.TaskType;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDate getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(LocalDate assignedDate) {
        this.assignedDate = assignedDate;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
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

    public String getCompletionRemarks() {
        return completionRemarks;
    }

    public void setCompletionRemarks(String completionRemarks) {
        this.completionRemarks = completionRemarks;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public OfficeStaff getStaff() {
        return staff;
    }

    public void setStaff(OfficeStaff staff) {
        this.staff = staff;
    }

    public TeamLead getTeamLead() {
        return teamLead;
    }

    public void setTeamLead(TeamLead teamLead) {
        this.teamLead = teamLead;
    }
}