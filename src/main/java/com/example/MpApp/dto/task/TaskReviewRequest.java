package com.example.MpApp.dto.task;

import com.example.MpApp.entity.enums.VerificationStatus;

public class TaskReviewRequest {

    private VerificationStatus verificationStatus;

    private String reviewComment;

    private Integer pointsDeduction;

    private String reworkNotes;

    public TaskReviewRequest() {
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

    public Integer getPointsDeduction() {
        return pointsDeduction;
    }

    public void setPointsDeduction(Integer pointsDeduction) {
        this.pointsDeduction = pointsDeduction;
    }

    public String getReworkNotes() {
        return reworkNotes;
    }

    public void setReworkNotes(String reworkNotes) {
        this.reworkNotes = reworkNotes;
    }
}