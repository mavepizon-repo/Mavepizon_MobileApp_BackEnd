package com.example.MpApp.entity.course;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "course_staff_assignments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_course_staff_assignment",
                        columnNames = {"course_id"}
                )
        }
)
public class CourseStaffAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ==========================================================
    // COURSE
    // ==========================================================

    /*
     * One Course has one staff-assignment configuration.
     *
     * That configuration can contain:
     *
     * ONLINE staff
     * TISAIYANVILAI staff
     * TIRUNELVELI staff
     */

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "course_id",
            nullable = false,
            unique = true
    )
    private Course course;


    // ==========================================================
    // ONLINE STAFF
    // ==========================================================

    @Column(name = "online_staff_id")
    private Long onlineStaffId;


    @Column(name = "online_zoom_link")
    private String onlineZoomLink;


    // ==========================================================
    // TISAIYANVILAI STAFF
    // ==========================================================

    @Column(name = "tisaiyanvilai_staff_id")
    private Long tisaiyanvilaiStaffId;


    @Column(name = "tisaiyanvilai_zoom_link")
    private String tisaiyanvilaiZoomLink;


    // ==========================================================
    // TIRUNELVELI STAFF
    // ==========================================================

    @Column(name = "tirunelveli_staff_id")
    private Long tirunelveliStaffId;


    @Column(name = "tirunelveli_zoom_link")
    private String tirunelveliZoomLink;


    // ==========================================================
    // AUDIT
    // ==========================================================

    @Column(name = "assigned_by")
    private String assignedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    // ==========================================================
    // CONSTRUCTOR
    // ==========================================================

    public CourseStaffAssignment() {
    }


    // ==========================================================
    // PRE PERSIST
    // ==========================================================

    @PrePersist
    public void prePersist() {

        LocalDateTime now =
                LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }


    // ==========================================================
    // PRE UPDATE
    // ==========================================================

    @PreUpdate
    public void preUpdate() {

        updatedAt =
                LocalDateTime.now();
    }


    // ==========================================================
    // GETTERS & SETTERS
    // ==========================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    // ==========================================================
    // COURSE
    // ==========================================================

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }


    // ==========================================================
    // ONLINE
    // ==========================================================

    public Long getOnlineStaffId() {
        return onlineStaffId;
    }

    public void setOnlineStaffId(Long onlineStaffId) {
        this.onlineStaffId =
                onlineStaffId;
    }


    public String getOnlineZoomLink() {
        return onlineZoomLink;
    }

    public void setOnlineZoomLink(
            String onlineZoomLink) {

        this.onlineZoomLink =
                onlineZoomLink;
    }


    // ==========================================================
    // TISAIYANVILAI
    // ==========================================================

    public Long getTisaiyanvilaiStaffId() {
        return tisaiyanvilaiStaffId;
    }

    public void setTisaiyanvilaiStaffId(
            Long tisaiyanvilaiStaffId) {

        this.tisaiyanvilaiStaffId =
                tisaiyanvilaiStaffId;
    }


    public String getTisaiyanvilaiZoomLink() {
        return tisaiyanvilaiZoomLink;
    }

    public void setTisaiyanvilaiZoomLink(
            String tisaiyanvilaiZoomLink) {

        this.tisaiyanvilaiZoomLink =
                tisaiyanvilaiZoomLink;
    }


    // ==========================================================
    // TIRUNELVELI
    // ==========================================================

    public Long getTirunelveliStaffId() {
        return tirunelveliStaffId;
    }

    public void setTirunelveliStaffId(
            Long tirunelveliStaffId) {

        this.tirunelveliStaffId =
                tirunelveliStaffId;
    }


    public String getTirunelveliZoomLink() {
        return tirunelveliZoomLink;
    }

    public void setTirunelveliZoomLink(
            String tirunelveliZoomLink) {

        this.tirunelveliZoomLink =
                tirunelveliZoomLink;
    }


    // ==========================================================
    // AUDIT
    // ==========================================================

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy =
                assignedBy;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt =
                createdAt;
    }


    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(
            LocalDateTime updatedAt) {

        this.updatedAt =
                updatedAt;
    }
}