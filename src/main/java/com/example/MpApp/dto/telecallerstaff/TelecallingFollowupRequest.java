package com.example.MpApp.dto.telecallerstaff;

import com.example.MpApp.entity.enums.EnquiryStatus;

import java.time.LocalDate;

public class TelecallingFollowupRequest {

    private LocalDate nextFollowupDate;

    private EnquiryStatus status;

    private String remarks;

    public TelecallingFollowupRequest() {
    }

    public LocalDate getNextFollowupDate() {
        return nextFollowupDate;
    }

    public void setNextFollowupDate(LocalDate nextFollowupDate) {
        this.nextFollowupDate = nextFollowupDate;
    }

    public EnquiryStatus getStatus() {
        return status;
    }

    public void setStatus(EnquiryStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}