package com.example.MpApp.controller.course;

import com.example.MpApp.config.JwtService;
import com.example.MpApp.entity.course.Course;
import com.example.MpApp.service.course.CourseService;
import com.example.MpApp.repository.teamlead.TeamLeadRepository;
import com.example.MpApp.repository.admin.AdminRepository;
import com.example.MpApp.entity.teamlead.TeamLead;
import com.example.MpApp.entity.admin.Admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/course")
@CrossOrigin("*")
public class CourseController {

    @Autowired
    private CourseService service;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TeamLeadRepository teamLeadRepository;

    @Autowired
    private AdminRepository adminRepository;

    /*
    ===================================
    RESOLVE CREATOR NAME FROM TOKEN (ADMIN OR TEAM LEAD)
    ===================================
    */
    private String getAuthorizedCreatorName(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7);
        String email = jwtService.extractEmail(token);

        // 1. Check if the user is an Admin
        Optional<Admin> admin = adminRepository.findByEmail(email);
        if (admin.isPresent()) {
            return admin.get().getUserName() + " (Admin)";
        }

        // 2. Check if the user is a Team Lead
        Optional<TeamLead> teamLead = teamLeadRepository.findByEmail(email);
        if (teamLead.isPresent()) {
            return teamLead.get().getName() + " (Team Lead)";
        }

        return null;
    }

    /*
    ===================================
    CREATE COURSE (ADMIN & TL ALLOWED)
    ===================================
    */
    @PostMapping("/create")
    public ResponseEntity<?> createCourse(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Course course) {

        String creatorName = getAuthorizedCreatorName(authHeader);

        if (creatorName == null) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: Only Admin or Team Lead allowed");
        }

        course.setCreatedBy(creatorName);

        return ResponseEntity.ok(service.createCourse(course));
    }

    /*
    ===================================
    GET ALL
    ===================================
    */
    @GetMapping("/get-all")
    public ResponseEntity<?> getAllCourses() {
        return ResponseEntity.ok(service.getAllCourses());
    }

    /*
    ===================================
    GET BY ID / CODE
    ===================================
    */
    @GetMapping("/get/code/{courseCode}")
    public ResponseEntity<?> getCourseByCourseCode(@PathVariable String courseCode) {
        return ResponseEntity.ok(service.getCourseByCourseCode(courseCode));
    }

    @GetMapping("/get/id/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getCourseById(id));
    }

    /*
    ===================================
    UPDATE (ADMIN & TL ALLOWED)
    ===================================
    */
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateCourse(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestBody Course course) {

        String creatorName = getAuthorizedCreatorName(authHeader);

        if (creatorName == null) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: Only Admin or Team Lead allowed to modify courses");
        }

        return ResponseEntity.ok(service.updateCourse(id, course));
    }

    /*
    ===================================
    DELETE (ADMIN & TL ALLOWED)
    ===================================
    */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteCourse(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {

        String creatorName = getAuthorizedCreatorName(authHeader);

        if (creatorName == null) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: Only Admin or Team Lead allowed to delete courses");
        }

        service.deleteCourse(id);
        return ResponseEntity.ok("Course Deleted Successfully");
    }
}