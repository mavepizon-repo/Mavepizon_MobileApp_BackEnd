package com.example.MpApp.entity.developer_trainer_staff;

import com.example.MpApp.entity.officestaff.OfficeStaff;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Entity
@Table(name="course_materials")
public class CourseMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String fileUrl;

    private LocalDateTime uploadedAt;

    @ManyToOne
    private TrainingBatch batch;

    @ManyToOne
    private OfficeStaff trainer;

    @PrePersist
    public void prePersist() {
        uploadedAt = LocalDateTime.now();
    }

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

    public TrainingBatch getBatch() {
        return batch;
    }

    public void setBatch(TrainingBatch batch) {
        this.batch = batch;
    }

    public OfficeStaff getTrainer() {
        return trainer;
    }

    public void setTrainer(OfficeStaff trainer) {
        this.trainer = trainer;
    }
}