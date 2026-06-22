package com.example.MpApp.entity.course;

import com.example.MpApp.entity.developer_trainer_staff.TrainingBatch;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "offered_courses")
public class OfferedCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     ==================================
     COURSE RELATIONSHIP
     ==================================
     */
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @JsonIgnore
    @OneToMany(
            mappedBy = "offeredCourse",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<StudentCourseRegistration> registrations =
            new ArrayList<>();

    /*
     ==================================
     BATCH CODE
     ==================================
     */
    private String batchCode;

    private String courseName;

    private String timing;

    private LocalDate startingDate;

    private LocalDate registrationEndingDate;

    private String courseDuration;

    private Double feesDetails;

    private Integer onlineSeats;

    private Integer offlineSeats;

    private Integer onlineFilledSeats;

    private Integer offlineFilledSeats;

    private Integer onlineRemainingSeats;

    private Integer offlineRemainingSeats;

    @Column(length = 2000)
    private String courseDescription;

    private String createdBy;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /*
     ==================================
     TRAINING BATCHES
     ==================================
     */
    @JsonIgnore
    @OneToMany(
            mappedBy = "offeredCourse",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<TrainingBatch> trainingBatches =
            new ArrayList<>();

    @PrePersist
    public void prePersist() {

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (onlineFilledSeats == null) {
            onlineFilledSeats = 0;
        }

        if (offlineFilledSeats == null) {
            offlineFilledSeats = 0;
        }

        if (onlineRemainingSeats == null) {
            onlineRemainingSeats = onlineSeats;
        }

        if (offlineRemainingSeats == null) {
            offlineRemainingSeats = offlineSeats;
        }

        if (status == null) {
            status = "ACTIVE";
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public OfferedCourse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public void setBatchCode(String batchCode) {
        this.batchCode = batchCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getTiming() {
        return timing;
    }

    public void setTiming(String timing) {
        this.timing = timing;
    }

    public LocalDate getStartingDate() {
        return startingDate;
    }

    public void setStartingDate(LocalDate startingDate) {
        this.startingDate = startingDate;
    }

    public LocalDate getRegistrationEndingDate() {
        return registrationEndingDate;
    }

    public void setRegistrationEndingDate(LocalDate registrationEndingDate) {
        this.registrationEndingDate = registrationEndingDate;
    }

    public String getCourseDuration() {
        return courseDuration;
    }

    public void setCourseDuration(String courseDuration) {
        this.courseDuration = courseDuration;
    }

    public Double getFeesDetails() {
        return feesDetails;
    }

    public void setFeesDetails(Double feesDetails) {
        this.feesDetails = feesDetails;
    }

    public Integer getOnlineSeats() {
        return onlineSeats;
    }

    public void setOnlineSeats(Integer onlineSeats) {
        this.onlineSeats = onlineSeats;
    }

    public Integer getOfflineSeats() {
        return offlineSeats;
    }

    public void setOfflineSeats(Integer offlineSeats) {
        this.offlineSeats = offlineSeats;
    }

    public Integer getOnlineFilledSeats() {
        return onlineFilledSeats;
    }

    public void setOnlineFilledSeats(Integer onlineFilledSeats) {
        this.onlineFilledSeats = onlineFilledSeats;
    }

    public Integer getOfflineFilledSeats() {
        return offlineFilledSeats;
    }

    public void setOfflineFilledSeats(Integer offlineFilledSeats) {
        this.offlineFilledSeats = offlineFilledSeats;
    }

    public Integer getOnlineRemainingSeats() {
        return onlineRemainingSeats;
    }

    public void setOnlineRemainingSeats(Integer onlineRemainingSeats) {
        this.onlineRemainingSeats = onlineRemainingSeats;
    }

    public Integer getOfflineRemainingSeats() {
        return offlineRemainingSeats;
    }

    public void setOfflineRemainingSeats(Integer offlineRemainingSeats) {
        this.offlineRemainingSeats = offlineRemainingSeats;
    }

    public String getCourseDescription() {
        return courseDescription;
    }

    public void setCourseDescription(String courseDescription) {
        this.courseDescription = courseDescription;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public List<TrainingBatch> getTrainingBatches() {
        return trainingBatches;
    }

    public void setTrainingBatches(
            List<TrainingBatch> trainingBatches) {
        this.trainingBatches = trainingBatches;
    }
}