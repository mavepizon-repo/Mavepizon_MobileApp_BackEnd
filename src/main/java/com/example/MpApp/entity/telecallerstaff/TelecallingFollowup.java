package com.example.MpApp.entity.telecallerstaff;

import com.example.MpApp.entity.enums.EnquiryStatus;
import com.example.MpApp.entity.telecallerstaff.TelecallingEnquiry;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "telecalling_followup")
public class TelecallingFollowup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     ==================================
     ENQUIRY RELATIONSHIP
     ==================================
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enquiry_id")
    private TelecallingEnquiry enquiry;

    private LocalDate followupDate;

    private LocalDate nextFollowupDate;

    @Enumerated(EnumType.STRING)
    private EnquiryStatus status;

    @Column(length = 2000)
    private String remarks;

    private String updatedBy;

    private LocalDateTime createdAt;

    /*
     ==================================
     AUTO TIMESTAMPS
     ==================================
     */

    @PrePersist
    public void prePersist() {

        followupDate = LocalDate.now();

        createdAt = LocalDateTime.now();
    }

    /*
     ==================================
     GETTERS & SETTERS
     ==================================
     */

    public Long getId() {
        return id;
    }

    public TelecallingEnquiry getEnquiry() {
        return enquiry;
    }

    public void setEnquiry(TelecallingEnquiry enquiry) {
        this.enquiry = enquiry;
    }

    public LocalDate getFollowupDate() {
        return followupDate;
    }

    public void setFollowupDate(LocalDate followupDate) {
        this.followupDate = followupDate;
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

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}