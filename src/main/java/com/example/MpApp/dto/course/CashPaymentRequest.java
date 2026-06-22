package com.example.MpApp.dto.course;

public class CashPaymentRequest {

    private Long registrationId; // Nullable if payment is for an Internship
    private Long internshipRegistrationId; // Added for Internship tracking
    private Double amount;
    private Long selectedStaffId;
    private String selectedStaffName;
    private String remarks;

    public CashPaymentRequest() {}

    public Long getRegistrationId() { return registrationId; }
    public void setRegistrationId(Long registrationId) { this.registrationId = registrationId; }

    public Long getInternshipRegistrationId() { return internshipRegistrationId; }
    public void setInternshipRegistrationId(Long internshipRegistrationId) { this.internshipRegistrationId = internshipRegistrationId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public Long getSelectedStaffId() { return selectedStaffId; }
    public void setSelectedStaffId(Long selectedStaffId) { this.selectedStaffId = selectedStaffId; }

    public String getSelectedStaffName() { return selectedStaffName; }
    public void setSelectedStaffName(String selectedStaffName) { this.selectedStaffName = selectedStaffName; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}