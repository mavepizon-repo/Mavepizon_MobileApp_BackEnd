package com.example.MpApp.service.course;

import com.example.MpApp.entity.course.Course;
import com.example.MpApp.repository.course.CourseRepository;
import com.example.MpApp.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CourseService {

    @Autowired
    private CourseRepository repository;

    /*
    ===================================
    CREATE CODES
    ===================================
    */

    private String generateCourseCode(String courseName) {
        String name = courseName.toUpperCase().trim();
        String prefix;

        if (name.contains("FULL STACK")) {
            prefix = "FS";
        }
        else if (name.contains("FLUTTER")) {
            prefix = "FL";
        }
        else if (name.contains("SPRING BOOT")) {
            prefix = "SP";
        }
        else if (name.contains("PYTHON")) {
            prefix = "PDS";
        }
        else if (name.contains("BLOCKCHAIN")) {
            prefix = "BLC";
        }
        else {
            throw new IllegalArgumentException("Unsupported course type: " + courseName);
        }

        String monthYear = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("MMyyyy"));

        String baseCode = prefix + monthYear;
        long count = repository.countByCourseCodeStartingWith(baseCode);

        return baseCode + String.format("%03d", count + 1);
    }

    // ================= CREATE METHOD (UPDATED) =================

    @Transactional
    public Map<String, String> createCourse(Course course) {
        String generatedCode = generateCourseCode(course.getCourseName());
        course.setCourseCode(generatedCode);

        if (course.getTotalFees() == null) {
            throw new IllegalArgumentException("Total Fee is required");
        }

        double registrationFee = course.getTotalFees() * 0.30;
        course.setRegistrationFees(registrationFee);

        Course savedCourse = repository.save(course);

        Map<String, String> response = new HashMap<>();
        response.put("courseId", savedCourse.getId().toString());
        response.put("courseCode", savedCourse.getCourseCode());
        response.put("message", "Course Created Successfully");
        return response;
    }

    /*
    ===================================
    GET ALL (UNTOUCHED)
    ===================================
    */

    public List<Course> getAllCourses() {
        return repository.findAll();
    }

    /*
    ===================================
    GET BY ID (UNTOUCHED)
    ===================================
    */

    public Course getCourseById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course Not Found"));
    }

    // ================= UPDATE METHOD (UPDATED) =================

    @Transactional
    public Map<String, String> updateCourse(Long id, Course course) {
        Course existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course Not Found"));

        if (course.getCourseCode() != null) {
            existing.setCourseCode(course.getCourseCode());
        }
        if (course.getCourseName() != null) {
            existing.setCourseName(course.getCourseName());
        }
        if (course.getDescription() != null) {
            existing.setDescription(course.getDescription());
        }
        if (course.getDuration() != null) {
            existing.setDuration(course.getDuration());
        }
        if (course.getStartDate() != null) {
            existing.setStartDate(course.getStartDate());
        }
        if (course.getEndDate() != null) {
            existing.setEndDate(course.getEndDate());
        }
        if (course.getTotalFees() != null) {
            existing.setTotalFees(course.getTotalFees());
            existing.setRegistrationFees(course.getTotalFees() * 0.30);
        }
        if (course.getTotalSeatsOnline() != null) {
            existing.setTotalSeatsOnline(course.getTotalSeatsOnline());
        }
        if (course.getAvailableSeatsOnline() != null) {
            existing.setAvailableSeatsOnline(course.getAvailableSeatsOnline());
        }
        if (course.getTotalSeatsOffline() != null) {
            existing.setTotalSeatsOffline(course.getTotalSeatsOffline());
        }
        if (course.getAvailableSeatsOffline() != null) {
            existing.setAvailableSeatsOffline(course.getAvailableSeatsOffline());
        }
        if (course.getStatus() != null) {
            existing.setStatus(course.getStatus());
        }
        if (course.getZoomLink() != null) {
            existing.setZoomLink(course.getZoomLink());
        }
        if (course.getTrainerName() != null) {
            existing.setTrainerName(course.getTrainerName());
        }

        repository.save(existing);

        Map<String, String> response = new HashMap<>();
        response.put("courseId", id.toString());
        response.put("message", "Course Updated Successfully");
        return response;
    }

    /*
    ===================================
    DELETE & EXTRA QUERY METHODS (UNTOUCHED)
    ===================================
    */

    public Optional<Course> getCourseByCourseCode(String courseCode){
        return repository.findByCourseCode(courseCode);
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course Not Found"));
        repository.delete(course);
    }
}