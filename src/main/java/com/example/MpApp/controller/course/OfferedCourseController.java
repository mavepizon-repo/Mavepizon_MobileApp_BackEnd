package com.example.MpApp.controller.course;

import com.example.MpApp.config.JwtService;
import com.example.MpApp.entity.course.OfferedCourse;
import com.example.MpApp.service.course.OfferedCourseService;
import com.example.MpApp.repository.teamlead.TeamLeadRepository;
import com.example.MpApp.entity.teamlead.TeamLead;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/offered-course")
@CrossOrigin("*")
public class OfferedCourseController {

    @Autowired
    private OfferedCourseService service;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TeamLeadRepository teamLeadRepository;

    private TeamLead validateTeamLead(
            String authHeader) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token =
                authHeader.substring(7);

        String email =
                jwtService.extractEmail(token);

        return teamLeadRepository
                .findByEmail(email)
                .orElse(null);
    }

    /*
    ===================================
    CREATE
    ===================================
    */

    @PostMapping("/create")
    public ResponseEntity<?> createCourse(

            @RequestHeader("Authorization")
            String authHeader,

            @RequestBody OfferedCourse course) {

        TeamLead teamLead =
                validateTeamLead(authHeader);

        if (teamLead == null) {

            return ResponseEntity
                    .status(403)
                    .body("Only Team Lead Allowed");
        }

        course.setCreatedBy(
                teamLead.getName());

        return ResponseEntity.ok(
                service.createCourse(course));
    }

    /*
    ===================================
    GET ALL
    ===================================
    */

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllCourses() {

        return ResponseEntity.ok(
                service.getAllCourses());
    }

    /*
    ===================================
    GET ACTIVE
    ===================================
    */

    @GetMapping("/active")
    public ResponseEntity<?> getActiveCourses() {

        return ResponseEntity.ok(
                service.getActiveCourses());
    }

    /*
    ===================================
    GET BY ID
    ===================================
    */

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getCourseById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.getCourseById(id));
    }

    /*
    ===================================
    UPDATE
    ===================================
    */

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateCourse(

            @RequestHeader("Authorization")
            String authHeader,

            @PathVariable Long id,

            @RequestBody OfferedCourse course) {

        TeamLead teamLead =
                validateTeamLead(authHeader);

        if (teamLead == null) {

            return ResponseEntity
                    .status(403)
                    .body("Only Team Lead Allowed");
        }

        return ResponseEntity.ok(
                service.updateCourse(
                        id,
                        course));
    }

    /*
    ===================================
    DELETE
    ===================================
    */

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteCourse(

            @RequestHeader("Authorization")
            String authHeader,

            @PathVariable Long id) {

        TeamLead teamLead =
                validateTeamLead(authHeader);

        if (teamLead == null) {

            return ResponseEntity
                    .status(403)
                    .body("Only Team Lead Allowed");
        }

        service.deleteCourse(id);

        return ResponseEntity.ok(
                "Offered Course Deleted Successfully");
    }
}