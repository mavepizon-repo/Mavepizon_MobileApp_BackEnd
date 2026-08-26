package com.example.MpApp.repository.course;

import com.example.MpApp.entity.course.StudentCourseRegistration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentCourseRegistrationRepository
        extends JpaRepository<StudentCourseRegistration, Long> {


    // =========================================================
    // STUDENT QUERIES
    // =========================================================

    List<StudentCourseRegistration>
    findByStudentStudentId(String studentId);


    Integer
    countByStudentStudentId(String studentId);


    // =========================================================
    // DUPLICATE REGISTRATION CHECK
    // =========================================================

    boolean
    existsByStudentStudentIdAndCourseId(
            String studentId,
            Long courseId
    );


    // =========================================================
    // FIND EXISTING REGISTRATION
    // =========================================================

    Optional<StudentCourseRegistration>
    findByStudentStudentIdAndCourseId(
            String studentId,
            Long courseId
    );


    // =========================================================
    // GET REGISTRATIONS BY COURSE
    // =========================================================

    List<StudentCourseRegistration>
    findByCourseId(Long courseId);


    // =========================================================
    // GET ALL WITH STUDENT + COURSE
    // =========================================================

    @Query("""
            SELECT r
            FROM StudentCourseRegistration r
            JOIN FETCH r.student
            JOIN FETCH r.course
            ORDER BY r.createdAt DESC
            """)
    List<StudentCourseRegistration>
    findAllWithStudentAndCourse();


    // =========================================================
    // GET ONE WITH STUDENT + COURSE
    // =========================================================

    @Query("""
            SELECT r
            FROM StudentCourseRegistration r
            JOIN FETCH r.student
            JOIN FETCH r.course
            WHERE r.id = :id
            """)
    Optional<StudentCourseRegistration>
    findByIdWithStudentAndCourse(
            @Param("id") Long id
    );


    // =========================================================
    // GET REGISTRATION BY ID + STUDENT
    // Used for PATCH update
    // =========================================================

    @Query("""
            SELECT r
            FROM StudentCourseRegistration r
            JOIN FETCH r.student
            JOIN FETCH r.course
            WHERE r.id = :registrationId
            AND r.student.studentId = :studentId
            """)
    Optional<StudentCourseRegistration>
    findByIdAndStudentStudentId(
            @Param("registrationId") Long registrationId,
            @Param("studentId") String studentId
    );


    // =========================================================
    // GET BY REGISTRATION STATUS
    // =========================================================

    List<StudentCourseRegistration>
    findByRegistrationStatus(
            String registrationStatus
    );


    // =========================================================
    // GET STUDENT CONFIRMED COURSES
    // =========================================================

    List<StudentCourseRegistration>
    findByStudentStudentIdAndRegistrationStatus(
            String studentId,
            String registrationStatus
    );
}