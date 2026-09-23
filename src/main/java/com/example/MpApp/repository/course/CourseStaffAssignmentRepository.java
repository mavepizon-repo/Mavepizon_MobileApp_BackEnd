package com.example.MpApp.repository.course;

import com.example.MpApp.entity.course.CourseStaffAssignment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseStaffAssignmentRepository
        extends JpaRepository<CourseStaffAssignment, Long> {

    Optional<CourseStaffAssignment> findByCourseId(Long courseId);

    boolean existsByCourseId(Long courseId);

    void deleteByCourseId(Long courseId);
}