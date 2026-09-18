package com.example.MpApp.entity.course;

import com.example.MpApp.entity.student.Student;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "student_course_registration",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_student_course",
                        columnNames = {
                                "student_id",
                                "course_id"
                        }
                )
        }
)
public class StudentCourseRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =========================================================
    // STUDENT
    // =========================================================

    @JsonIgnoreProperties({
            "registrations",
            "cashPayments",
            "hibernateLazyInitializer",
            "handler"
    })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "student_id",
            nullable = false
    )
    private Student student;


    // =========================================================
    // COURSE
    // =========================================================

    @JsonIgnoreProperties({
            "hibernateLazyInitializer",
            "handler"
    })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "course_id",
            nullable = false
    )
    private Course course;


    // =========================================================
    // MODE
    // =========================================================

    /*
     * ONLINE
     * OFFLINE
     */

    @Column(nullable = false)
    private String mode;


    // =========================================================
    // LOCATION
    // =========================================================

    /*
     * ONLINE
     *     -> null
     *
     * OFFLINE
     *     -> TIRUNELVELI
     *     -> TISAIYANVILAI
     */

    private String location;


    // =========================================================
    // PAYMENT
    // =========================================================

    /*
     * PENDING
     * PAID
     * FAILED
     */

    private String paymentStatus;


    // =========================================================
    // REGISTRATION STATUS
    // =========================================================

    /*
     * PENDING_PAYMENT
     * CONFIRMED
     * PAYMENT_FAILED
     * CANCELLED
     */

    @Column(nullable = false)
    private String registrationStatus;


    // =========================================================
    // REGISTRATION FEE
    // =========================================================

    /*
     * Registration amount calculated by backend.
     *
     * Frontend must NOT decide the amount.
     */

    private Double registrationFeeAmount;


    // =========================================================
    // CERTIFICATE
    // =========================================================

    private String certificateStatus;


    // =========================================================
    // STUDENT COURSE COUNT
    // =========================================================

    private Integer registeredCoursesCount;


    // =========================================================
    // DATE / TIME
    // =========================================================

    private LocalDate registrationDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public StudentCourseRegistration() {
    }


    // =========================================================
    // PRE PERSIST
    // =========================================================

    @PrePersist
    public void prePersist() {

        if (registrationDate == null) {
            registrationDate = LocalDate.now();
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        updatedAt = LocalDateTime.now();


        if (paymentStatus == null) {
            paymentStatus = "PENDING";
        }


        if (registrationStatus == null) {
            registrationStatus = "PENDING_PAYMENT";
        }


        if (certificateStatus == null) {
            certificateStatus = "NOT_GENERATED";
        }


        if (registeredCoursesCount == null) {
            registeredCoursesCount = 1;
        }
    }


    // =========================================================
    // PRE UPDATE
    // =========================================================

    @PreUpdate
    public void preUpdate() {

        updatedAt = LocalDateTime.now();
    }


    // =========================================================
    // GETTERS AND SETTERS
    // =========================================================

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


    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }


    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }


    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }


    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }


    public String getRegistrationStatus() {
        return registrationStatus;
    }

    public void setRegistrationStatus(
            String registrationStatus) {

        this.registrationStatus =
                registrationStatus;
    }


    public Double getRegistrationFeeAmount() {
        return registrationFeeAmount;
    }

    public void setRegistrationFeeAmount(
            Double registrationFeeAmount) {

        this.registrationFeeAmount =
                registrationFeeAmount;
    }


    public String getCertificateStatus() {
        return certificateStatus;
    }

    public void setCertificateStatus(
            String certificateStatus) {

        this.certificateStatus =
                certificateStatus;
    }


    public Integer getRegisteredCoursesCount() {
        return registeredCoursesCount;
    }

    public void setRegisteredCoursesCount(
            Integer registeredCoursesCount) {

        this.registeredCoursesCount =
                registeredCoursesCount;
    }


    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(
            LocalDate registrationDate) {

        this.registrationDate =
                registrationDate;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt = createdAt;
    }


    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(
            LocalDateTime updatedAt) {

        this.updatedAt = updatedAt;
    }
}