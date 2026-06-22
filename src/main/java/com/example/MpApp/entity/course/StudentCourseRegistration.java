package com.example.MpApp.entity.course;

import com.example.MpApp.entity.student.Student;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_course_registration")
public class StudentCourseRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     ==========================
     STUDENT RELATION
     ==========================
     */

    /*
     ==================================
     RELATIONSHIPS
     ==================================
     */

    @JsonIgnoreProperties({"registrations", "cashPayments", "hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @JsonIgnoreProperties({"registrations", "hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offered_course_id", nullable = false)
    private OfferedCourse offeredCourse;

    /*
     ==========================
     REGISTRATION DETAILS
     ==========================
     */

    private String gender;

    private LocalDate dob;

    private String year;

    @Column(length = 1000)
    private String address;

    private String profileImage;

    private String mode;

    private String paymentFor;

    private String paymentStatus;

    private String certificateStatus;

    private Integer registeredCoursesCount;

    private LocalDate registrationDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public StudentCourseRegistration() {
    }

    @PrePersist
    public void prePersist() {

        registrationDate = LocalDate.now();

        createdAt = LocalDateTime.now();

        updatedAt = LocalDateTime.now();

        if (paymentStatus == null) {
            paymentStatus = "UNPAID";
        }

        if (certificateStatus == null) {
            certificateStatus = "NOT_GENERATED";
        }

        if (registeredCoursesCount == null) {
            registeredCoursesCount = 1;
        }
    }

    @PreUpdate
    public void preUpdate() {

        updatedAt = LocalDateTime.now();
    }

    // ==========================
    // GETTERS & SETTERS
    // ==========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public OfferedCourse getOfferedCourse() {
        return offeredCourse;
    }

    public void setOfferedCourse(OfferedCourse offeredCourse) {
        this.offeredCourse = offeredCourse;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getPaymentFor() {
        return paymentFor;
    }

    public void setPaymentFor(String paymentFor) {
        this.paymentFor = paymentFor;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getCertificateStatus() {
        return certificateStatus;
    }

    public void setCertificateStatus(String certificateStatus) {
        this.certificateStatus = certificateStatus;
    }

    public Integer getRegisteredCoursesCount() {
        return registeredCoursesCount;
    }

    public void setRegisteredCoursesCount(Integer registeredCoursesCount) {
        this.registeredCoursesCount = registeredCoursesCount;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}