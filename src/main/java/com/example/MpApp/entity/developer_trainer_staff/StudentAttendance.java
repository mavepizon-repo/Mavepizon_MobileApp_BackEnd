package com.example.MpApp.entity.developer_trainer_staff;

import com.example.MpApp.entity.student.Student;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Optional;

@Entity
@Table(name="student_attendance")
public class StudentAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private TrainingBatch batch;

    @ManyToOne
    private Student student;

    private LocalDate attendanceDate;

    private Boolean present;

    public StudentAttendance() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TrainingBatch getBatch() {
        return batch;
    }


    public Student getStudent() {
        return student;
    }

    public void setBatch(TrainingBatch batch) {
        this.batch = batch;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public Boolean getPresent() {
        return present;
    }

    public void setPresent(Boolean present) {
        this.present = present;
    }

    public boolean isPresent() {
        return this.present;
    }


    // getters setters
}
