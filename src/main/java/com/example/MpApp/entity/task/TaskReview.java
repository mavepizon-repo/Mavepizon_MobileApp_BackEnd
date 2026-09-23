package com.example.MpApp.entity.task;

import com.example.MpApp.entity.teamlead.TeamLead;
import com.example.MpApp.entity.enums.VerificationStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_reviews")
public class TaskReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne
    @JoinColumn(name = "team_lead_id")
    private TeamLead reviewedBy;

    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus;

    @Column(length = 1000)
    private String reviewComment;

    @Column(length = 1000)
    private String reworkNotes;

    private Integer pointsDeduction;

    private LocalDateTime reviewedAt;

    public TaskReview() {
    }

    public Long getId() {
        return id;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public TeamLead getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(TeamLead reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(
            VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public String getReworkNotes() {
        return reworkNotes;
    }

    public void setReworkNotes(String reworkNotes) {
        this.reworkNotes = reworkNotes;
    }

    public Integer getPointsDeduction() {
        return pointsDeduction;
    }

    public void setPointsDeduction(Integer pointsDeduction) {
        this.pointsDeduction = pointsDeduction;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}