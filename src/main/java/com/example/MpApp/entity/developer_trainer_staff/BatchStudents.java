package com.example.MpApp.entity.developer_trainer_staff;

import com.example.MpApp.entity.student.Student;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name="batch_students")
public class BatchStudents {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="batch_id")
    private TrainingBatch batch;

    @ManyToOne
    @JoinColumn(name="student_id")
    private Student student;

    private LocalDate enrolledDate;

    public BatchStudents() {
    }

    // =========================================
    // GETTERS AND SETTERS
    // =========================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TrainingBatch getBatch() {
        return batch;
    }

    public void setBatch(TrainingBatch batch) {
        this.batch = batch;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public LocalDate getEnrolledDate() {
        return enrolledDate;
    }

    public void setEnrolledDate(LocalDate enrolledDate) {
        this.enrolledDate = enrolledDate;
    }
}