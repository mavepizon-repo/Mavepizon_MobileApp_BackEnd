package com.example.MpApp.repository.course;

import com.example.MpApp.entity.course.OfferedCourse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferedCourseRepository
        extends JpaRepository<OfferedCourse, Long> {

    List<OfferedCourse> findByStatus(
            String status);

    List<OfferedCourse> findByCourseNameContainingIgnoreCase(
            String courseName);
}