package com.example.MpApp.repository.course;

import com.example.MpApp.entity.course.Category;
import com.example.MpApp.entity.course.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository
        extends JpaRepository<Course, Long> {

    // =========================================================
    // FIND BY COURSE CODE
    // =========================================================

    Optional<Course> findByCourseCode(String courseCode);


    // =========================================================
    // FIND BY BATCH ID
    // =========================================================

    Optional<Course> findByBatchId(String batchId);


    // =========================================================
    // GENERATE NEXT BATCH NUMBER
    // =========================================================

    long countByBatchIdStartingWith(String prefix);


    // =========================================================
    // GET LAST COURSE
    // =========================================================

    Optional<Course> findTopByOrderByIdDesc();

    Optional<Course> findTopByCourseCodeStartingWithOrderByCourseCodeDesc(
            String prefix
    );

    List<Course> findByCategory(Category category);

    Optional<Course> findByIdAndCategory(Long id, Category category);

    Optional<Course> findByCourseCodeAndCategory(String courseCode, Category category);

    Optional<Course> findByBatchIdAndCategory(String batchId, Category category);
}