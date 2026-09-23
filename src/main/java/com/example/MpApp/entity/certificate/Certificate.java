package com.example.MpApp.entity.certificate;

import com.example.MpApp.entity.course.StudentCourseRegistration;
import com.example.MpApp.entity.internship.InternshipRegistration;
import com.example.MpApp.entity.student.Student;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "certificates")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =========================================================
    // STUDENT
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "student_id",
            nullable = false
    )
    @JsonIgnoreProperties({
            "hibernateLazyInitializer",
            "handler",
            "registrations",
            "certificates"
    })
    private Student student;


    // =========================================================
    // COURSE REGISTRATION
    // =========================================================

    /*
     * This connects the certificate to:
     *
     * Student
     *     ↓
     * StudentCourseRegistration
     *     ↓
     * Course
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_registration_id")
    @JsonIgnoreProperties({
            "hibernateLazyInitializer",
            "handler",
            "student"
    })
    private StudentCourseRegistration courseRegistration;


    // =========================================================
    // INTERNSHIP REGISTRATION
    // =========================================================

    /*
     * Kept because your application also has
     * internship certificates.
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_registration_id")
    @JsonIgnoreProperties({
            "hibernateLazyInitializer",
            "handler",
            "student",
            "internship"
    })
    private InternshipRegistration internshipRegistration;


    // =========================================================
    // CERTIFICATE DETAILS
    // =========================================================

    /*
     * COURSE
     * INTERNSHIP
     */

    @Column(nullable = false)
    private String recordType;


    /*
     * PENDING
     * PROCESSING
     * ISSUED
     */

    @Column(nullable = false)
    private String status = "PENDING";


    @Column(length = 2000)
    private String fileUrl;


    private LocalDate issueDate;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public Certificate() {
    }


    // =========================================================
    // GETTERS & SETTERS
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


    public StudentCourseRegistration getCourseRegistration() {
        return courseRegistration;
    }

    public void setCourseRegistration(
            StudentCourseRegistration courseRegistration) {

        this.courseRegistration =
                courseRegistration;
    }


    public InternshipRegistration getInternshipRegistration() {
        return internshipRegistration;
    }

    public void setInternshipRegistration(
            InternshipRegistration internshipRegistration) {

        this.internshipRegistration =
                internshipRegistration;
    }


    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }


    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }
}