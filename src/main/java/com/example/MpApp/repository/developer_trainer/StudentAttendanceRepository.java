package com.example.MpApp.repository.developer_trainer;

import com.example.MpApp.entity.developer_trainer_staff.StudentAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StudentAttendanceRepository
        extends JpaRepository<StudentAttendance, Long> {


    // =========================================================
    // GET ATTENDANCE BY COURSE
    // =========================================================

    List<StudentAttendance> findByCourseId(
            Long courseId
    );


    // =========================================================
    // COUNT PRESENT STUDENTS FOR A COURSE ON A DATE
    // =========================================================

    Long countByCourseIdAndAttendanceDateAndPresent(
            Long courseId,
            LocalDate attendanceDate,
            Boolean present
    );


    // =========================================================
    // GET ATTENDANCE FOR A COURSE ON A PARTICULAR DATE
    // =========================================================

    List<StudentAttendance> findByCourseIdAndAttendanceDate(
            Long courseId,
            LocalDate attendanceDate
    );


    // =========================================================
    // COUNT ATTENDANCE RECORDS FOR A DATE
    // =========================================================

    Long countByAttendanceDate(
            LocalDate attendanceDate
    );
}