package com.example.MpApp.entity.developer_trainer_staff;

import com.example.MpApp.entity.course.Course;
import com.example.MpApp.entity.student.Student;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "student_attendance",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_student_course_date",
                        columnNames = {
                                "student_id",
                                "course_id",
                                "attendance_date"
                        }
                )
        }
)
public class StudentAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =========================================================
    // COURSE
    // =========================================================
    /*
     * Attendance belongs directly to a Course.
     *
     * TrainingBatch is no longer used.
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "course_id",
            nullable = false
    )
    private Course course;


    // =========================================================
    // STUDENT
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "student_id",
            nullable = false
    )
    private Student student;


    // =========================================================
    // ATTENDANCE DATE
    // =========================================================

    @Column(
            name = "attendance_date",
            nullable = false
    )
    private LocalDate attendanceDate;


    // =========================================================
    // PRESENT / ABSENT
    // =========================================================

    @Column(nullable = false)
    private Boolean present;


    // =========================================================
    // STAFF WHO MARKED ATTENDANCE
    // =========================================================
    /*
     * Developer + Trainer staff member who marked
     * this attendance.
     */

    @Column(name = "marked_by_staff_id")
    private Long markedByStaffId;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public StudentAttendance() {
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


    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }


    public Student getStudent() {
        return student;
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
        return Boolean.TRUE.equals(present);
    }


    public Long getMarkedByStaffId() {
        return markedByStaffId;
    }

    public void setMarkedByStaffId(Long markedByStaffId) {
        this.markedByStaffId = markedByStaffId;
    }
}