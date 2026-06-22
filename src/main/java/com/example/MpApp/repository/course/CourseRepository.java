package com.example.MpApp.repository.course;

import com.example.MpApp.entity.course.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseRepository
        extends JpaRepository<Course, Long> {

    Optional<Course> findByCourseCode(
            String courseCode);

    boolean existsByCourseCode(String courseCode);

    long countByCourseCodeStartingWith(String prefix);
}