package com.example.MpApp.repository.course;

import com.example.MpApp.entity.course.StudentCourseRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentCourseRegistrationRepository extends JpaRepository<StudentCourseRegistration, Long> {

    /*
     ==================================
     STUDENT QUERIES
     ==================================
     */
    List<StudentCourseRegistration> findByStudentStudentId(String studentId);

    Integer countByStudentStudentId(String studentId);

    /*
     ==================================
     DUPLICATE CHECK
     ==================================
     */
    boolean existsByStudentStudentIdAndOfferedCourseId(String studentId, Long offeredCourseId);

    /*
     ==================================
     JOIN FETCH QUERIES
     ==================================
     */
    @Query("""
            SELECT r
            FROM StudentCourseRegistration r
            JOIN FETCH r.student
            JOIN FETCH r.offeredCourse
            """)
    List<StudentCourseRegistration> findAllWithStudentAndCourse();

    @Query("""
            SELECT r
            FROM StudentCourseRegistration r
            JOIN FETCH r.student
            JOIN FETCH r.offeredCourse
            WHERE r.id = :id
            """)
    Optional<StudentCourseRegistration> findByIdWithStudentAndCourse(@Param("id") Long id);
    @Query("""
            SELECT r 
            FROM StudentCourseRegistration r 
            JOIN FETCH r.student s 
            JOIN FETCH r.offeredCourse c 
            WHERE c.id = (SELECT tb.offeredCourse.id FROM TrainingBatch tb WHERE tb.id = :batchId) 
            AND s.id IN (SELECT bs.student.id FROM BatchStudents bs WHERE bs.batch.id = :batchId)
            """)
    List<StudentCourseRegistration> findRegistrationsByBatchId(@Param("batchId") Long batchId);
}