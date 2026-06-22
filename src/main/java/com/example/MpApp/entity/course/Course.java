package com.example.MpApp.entity.course;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String courseCode;

    private String courseName;

    @Column(length = 2000)
    private String description;

    private String duration;

    private LocalDate startDate;

    private LocalDate endDate;

    private Double totalFees;

    private Double registrationFees;

    private Integer totalSeatsOnline;

    private Integer availableSeatsOnline;

    private Integer totalSeatsOffline;

    private Integer availableSeatsOffline;

    private String status;

    private String zoomLink;

    private String trainerName;

    private String createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;



    /*
     ==================================
     OFFERED COURSES
     ==================================
     */
    @OneToMany(
            mappedBy = "course",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<OfferedCourse> offeredCourses =
            new ArrayList<>();

    public Course() {
    }

    @PrePersist
    public void prePersist() {

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (status == null) {
            status = "ACTIVE";
        }

        if (availableSeatsOnline == null) {
            availableSeatsOnline = totalSeatsOnline;
        }

        if (availableSeatsOffline == null) {
            availableSeatsOffline = totalSeatsOffline;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Double getTotalFees() {
        return totalFees;
    }

    public void setTotalFees(Double totalFees) {
        this.totalFees = totalFees;
    }

    public Double getRegistrationFees() {
        return registrationFees;
    }

    public void setRegistrationFees(Double registrationFees) {
        this.registrationFees = registrationFees;
    }

    public Integer getTotalSeatsOnline() {
        return totalSeatsOnline;
    }



    public void setTotalSeatsOnline(Integer totalSeatsOnline) {
        this.totalSeatsOnline = totalSeatsOnline;
    }

    public Integer getAvailableSeatsOnline() {
        return availableSeatsOnline;
    }

    public void setAvailableSeatsOnline(Integer availableSeatsOnline) {
        this.availableSeatsOnline = availableSeatsOnline;
    }

    public Integer getTotalSeatsOffline() {
        return totalSeatsOffline;
    }

    public void setTotalSeatsOffline(Integer totalSeatsOffline) {
        this.totalSeatsOffline = totalSeatsOffline;
    }

    public Integer getAvailableSeatsOffline() {
        return availableSeatsOffline;
    }

    public void setAvailableSeatsOffline(Integer availableSeatsOffline) {
        this.availableSeatsOffline = availableSeatsOffline;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getZoomLink() {
        return zoomLink;
    }

    public void setZoomLink(String zoomLink) {
        this.zoomLink = zoomLink;
    }

    public String getTrainerName() {
        return trainerName;
    }

    public void setTrainerName(String trainerName) {
        this.trainerName = trainerName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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



    public List<OfferedCourse> getOfferedCourses() {
        return offeredCourses;
    }

    public void setOfferedCourses(
            List<OfferedCourse> offeredCourses) {
        this.offeredCourses = offeredCourses;
    }
}