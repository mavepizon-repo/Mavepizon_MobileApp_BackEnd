package com.example.MpApp.entity.course;

import com.example.MpApp.entity.student.Student;
import com.example.MpApp.entity.internship.InternshipRegistration; // Imported context
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cash_payments")
public class CashPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cashPaymentId;

    /*
     ==================================
     RELATIONSHIPS
     ==================================
     */

    @JsonIgnoreProperties({"cashPayments", "registrations", "hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // CHANGED: Made nullable = true so internship-only payments don't fail validation
    @JsonIgnoreProperties({"cashPayments", "student", "offeredCourse", "hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", nullable = true)
    private StudentCourseRegistration registration;

    // Tied to our dynamic module tracking fields
    @Column(name = "internship_registration_id")
    private Long internshipRegistrationId;

    private Double amount;
    private Long selectedStaffId;
    private String selectedStaffName;
    private String approvedBy;
    private LocalDateTime requestDate;
    private LocalDateTime approvalDate;
    private String status;

    @Column(length = 1000)
    private String remarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        cashPaymentId = "CP" + System.currentTimeMillis();
        requestDate = LocalDateTime.now();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (status == null) {
            status = "PENDING";
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ==================================
    // GETTERS & SETTERS (Including Internship Tracker)
    // ==================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCashPaymentId() { return cashPaymentId; }
    public void setCashPaymentId(String cashPaymentId) { this.cashPaymentId = cashPaymentId; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public StudentCourseRegistration getRegistration() { return registration; }
    public void setRegistration(StudentCourseRegistration registration) { this.registration = registration; }

    public Long getInternshipRegistrationId() { return internshipRegistrationId; }
    public void setInternshipRegistrationId(Long internshipRegistrationId) { this.internshipRegistrationId = internshipRegistrationId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public Long getSelectedStaffId() { return selectedStaffId; }
    public void setSelectedStaffId(Long selectedStaffId) { this.selectedStaffId = selectedStaffId; }

    public String getSelectedStaffName() { return selectedStaffName; }
    public void setSelectedStaffName(String selectedStaffName) { this.selectedStaffName = selectedStaffName; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }

    public LocalDateTime getApprovalDate() { return approvalDate; }
    public void setApprovalDate(LocalDateTime approvalDate) { this.approvalDate = approvalDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}