package com.example.MpApp.entity.developer_trainer_staff;

import com.example.MpApp.entity.course.Course;
import com.example.MpApp.entity.course.OfferedCourse;
import com.example.MpApp.entity.officestaff.OfficeStaff;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "training_batch")
public class TrainingBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     ==================================
     BATCH DETAILS
     ==================================
     */

    private String batchName;

    private String batchMode; // ONLINE / OFFLINE

    private String zoomLink;

    private LocalDate startDate;

    private LocalDate endDate;

    /*
     ==================================
     TRAINER
     ==================================
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id")
    @JsonIgnoreProperties({"batches", "password", "hibernateLazyInitializer", "handler"})
    private OfficeStaff trainer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offered_course_id")
    @JsonIgnoreProperties({"registrations", "hibernateLazyInitializer", "handler"})
    private OfferedCourse offeredCourse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id") // Optional link to general course
    @JsonIgnoreProperties({"offeredCourses", "hibernateLazyInitializer", "handler"})
    private Course course;

    public TrainingBatch() {
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

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public String getBatchMode() {
        return batchMode;
    }

    public void setBatchMode(String batchMode) {
        this.batchMode = batchMode;
    }

    public String getZoomLink() {
        return zoomLink;
    }

    public void setZoomLink(String zoomLink) {
        this.zoomLink = zoomLink;
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

    public OfficeStaff getTrainer() {
        return trainer;
    }

    public void setTrainer(OfficeStaff trainer) {
        this.trainer = trainer;
    }

    public OfferedCourse getOfferedCourse() {
        return offeredCourse;
    }

    public void setOfferedCourse(
            OfferedCourse offeredCourse) {
        this.offeredCourse = offeredCourse;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }
}