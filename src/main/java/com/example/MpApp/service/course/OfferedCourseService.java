package com.example.MpApp.service.course;

import com.example.MpApp.entity.course.OfferedCourse;
import com.example.MpApp.repository.course.OfferedCourseRepository;
import com.example.MpApp.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OfferedCourseService {

    @Autowired
    private OfferedCourseRepository repository;

    /*
    ===================================
    CREATE (UPDATED RESPONSE)
    ===================================
    */

    @Transactional
    public Map<String, String> createCourse(OfferedCourse course) {

        // Interior seat logic kept completely intact
        course.setOnlineFilledSeats(0);
        course.setOfflineFilledSeats(0);

        course.setOnlineRemainingSeats(course.getOnlineSeats());
        course.setOfflineRemainingSeats(course.getOfflineSeats());

        OfferedCourse savedCourse = repository.save(course);

        Map<String, String> response = new HashMap<>();
        response.put("offeredCourseId", savedCourse.getId().toString());
        response.put("message", "Offered Course Created Successfully");
        return response;
    }

    /*
    ===================================
    GET ALL (UNTOUCHED)
    ===================================
    */

    public List<OfferedCourse> getAllCourses() {
        return repository.findAll();
    }

    /*
    ===================================
    GET BY ID (UNTOUCHED)
    ===================================
    */

    public OfferedCourse getCourseById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offered Course Not Found"));
    }

    /*
    ===================================
    GET ACTIVE COURSES (UNTOUCHED)
    ===================================
    */

    public List<OfferedCourse> getActiveCourses() {
        return repository.findByStatus("ACTIVE");
    }

    /*
    ===================================
    UPDATE (UPDATED RESPONSE)
    ===================================
    */

    @Transactional
    public Map<String, String> updateCourse(Long id, OfferedCourse course) {

        OfferedCourse existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course Not Found"));

        // Interior update logic kept completely intact
        if (course.getCourseName() != null) {
            existing.setCourseName(course.getCourseName());
        }

        if (course.getTiming() != null) {
            existing.setTiming(course.getTiming());
        }

        if (course.getStartingDate() != null) {
            existing.setStartingDate(course.getStartingDate());
        }

        if (course.getRegistrationEndingDate() != null) {
            existing.setRegistrationEndingDate(course.getRegistrationEndingDate());
        }

        if (course.getCourseDuration() != null) {
            existing.setCourseDuration(course.getCourseDuration());
        }

        if (course.getFeesDetails() != null) {
            existing.setFeesDetails(course.getFeesDetails());
        }

        if (course.getOnlineSeats() != null) {
            existing.setOnlineSeats(course.getOnlineSeats());
        }

        if (course.getOfflineSeats() != null) {
            existing.setOfflineSeats(course.getOfflineSeats());
        }

        if (course.getCourseDescription() != null) {
            existing.setCourseDescription(course.getCourseDescription());
        }

        if (course.getStatus() != null) {
            existing.setStatus(course.getStatus());
        }

        repository.save(existing);

        Map<String, String> response = new HashMap<>();
        response.put("offeredCourseId", id.toString());
        response.put("message", "Offered Course Updated Successfully");
        return response;
    }

    /*
    ===================================
    DELETE (UNTOUCHED)
    ===================================
    */

    @Transactional
    public void deleteCourse(Long id) {
        OfferedCourse course = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course Not Found"));

        repository.delete(course);
    }
}