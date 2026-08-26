package com.example.MpApp.entity.developer_trainer_staff;

import com.example.MpApp.entity.course.Course;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_materials")
public class CourseMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =========================================================
    // MATERIAL DETAILS
    // =========================================================

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String fileUrl;

    private LocalDateTime uploadedAt;


    // =========================================================
    // COURSE
    // =========================================================
    /*
     * Material belongs to a COURSE.
     *
     * TrainingBatch is no longer used here.
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "course_id",
            nullable = false
    )
    private Course course;


    // =========================================================
    // UPLOADED BY STAFF
    // =========================================================
    /*
     * ID of the Developer + Trainer staff
     * who uploaded the material.
     *
     * We keep the ID instead of OfficeStaff because
     * the trainer/developer staff module is separate.
     */

    @Column(name = "uploaded_by_staff_id", nullable = false)
    private Long uploadedByStaffId;


    // =========================================================
    // CREATED DATE
    // =========================================================

    @PrePersist
    public void prePersist() {

        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
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


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }


    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }


    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }


    public Long getUploadedByStaffId() {
        return uploadedByStaffId;
    }

    public void setUploadedByStaffId(Long uploadedByStaffId) {
        this.uploadedByStaffId = uploadedByStaffId;
    }
}