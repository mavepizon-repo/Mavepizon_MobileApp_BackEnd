package com.example.MpApp.entity.telecallerstaff;

import com.example.MpApp.entity.enums.EnquiryStatus;
import com.example.MpApp.entity.student.Student;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "telecalling_enquiry")
public class TelecallingEnquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentName;

    private String phone;

    private String email;

    private String collegeName;

    private String department;

    private String city;


    private String address;

    public String getDistrict() {
        return district;
    }



    private String district;

    private String interestedCourse;

    private LocalDate enquiryDate;

    @Enumerated(EnumType.STRING)
    private EnquiryStatus status;

    @Column(length = 2000)
    private String remarks;

    /*
     ==================================
     STUDENT RELATIONSHIP
     ==================================
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    /*
     ==================================
     FOLLOWUPS
     ==================================
     */


    private LocalDate nextFollowupDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /*
     ==================================
     AUTO TIMESTAMPS
     ==================================
     */

    @PrePersist
    public void prePersist() {

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (status == null) {
            status = EnquiryStatus.NEW;
        }

        if (enquiryDate == null) {
            enquiryDate = LocalDate.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /*
     ==================================
     GETTERS & SETTERS
     ==================================
     */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getInterestedCourse() {
        return interestedCourse;
    }

    public void setInterestedCourse(String interestedCourse) {
        this.interestedCourse = interestedCourse;
    }

    public LocalDate getEnquiryDate() {
        return enquiryDate;
    }

    public void setEnquiryDate(LocalDate enquiryDate) {
        this.enquiryDate = enquiryDate;
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

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setDistrict(String district) {
        this.district = district;
    }
    public LocalDate getNextFollowupDate() {
        return nextFollowupDate;
    }

    public void setNextFollowupDate(LocalDate nextFollowupDate) {
        this.nextFollowupDate = nextFollowupDate;
    }


}