package com.example.MpApp.controller.course;

import com.example.MpApp.config.JwtService;
import com.example.MpApp.entity.admin.Admin;
import com.example.MpApp.entity.course.Course;
import com.example.MpApp.entity.teamlead.TeamLead;
import com.example.MpApp.repository.admin.AdminRepository;
import com.example.MpApp.repository.teamlead.TeamLeadRepository;
import com.example.MpApp.service.course.CourseService;

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


    // =========================================================
    // RESOLVE CREATOR NAME
    // ADMIN OR TEAM LEAD
    // =========================================================

    private String getAuthorizedCreatorName(String authHeader) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            return null;
        }

        try {

            String token = authHeader.substring(7);

            String email = jwtService.extractEmail(token);

            if (email == null || email.isBlank()) {
                return null;
            }


            // =================================================
            // CHECK ADMIN
            // =================================================

            Optional<Admin> admin =
                    adminRepository.findByEmail(email);

            if (admin.isPresent()) {

                return admin.get().getUserName()
                        + " (Admin)";
            }


            // =================================================
            // CHECK TEAM LEAD
            // =================================================

            Optional<TeamLead> teamLead =
                    teamLeadRepository.findByEmail(email);

            if (teamLead.isPresent()) {

                return teamLead.get().getName()
                        + " (Team Lead)";
            }

        } catch (Exception e) {

            return null;
        }

        return null;
    }


    // =========================================================
    // CREATE COURSE
    // ADMIN + TEAM LEAD
    // =========================================================

    @PostMapping("/create")
    public ResponseEntity<?> createCourse(

            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader,

            @RequestBody Course course) {

        String creatorName =
                getAuthorizedCreatorName(authHeader);

        if (creatorName == null) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "Access Denied: Only Admin or Team Lead allowed"
                    );
        }

        // Backend decides creator
        course.setCreatedBy(creatorName);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        service.createCourse(course)
                );
    }


    // =========================================================
    // GET ALL COURSES
    // =========================================================

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllCourses() {

        return ResponseEntity.ok(
                service.getAllCourses()
        );
    }


    // =========================================================
    // GET COURSE BY DATABASE ID
    // =========================================================

    @GetMapping("/get/id/{id}")
    public ResponseEntity<?> getCourseById(

            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.getCourseById(id)
        );
    }


    // =========================================================
    // GET COURSE BY COURSE CODE
    // =========================================================

    @GetMapping("/get/code/{courseCode}")
    public ResponseEntity<?> getCourseByCourseCode(

            @PathVariable String courseCode) {

        return ResponseEntity.ok(
                service.getCourseByCourseCode(courseCode)
        );
    }


    // =========================================================
    // GET COURSE BY BATCH ID
    // =========================================================

    @GetMapping("/get/batch/{batchId}")
    public ResponseEntity<?> getCourseByBatchId(

            @PathVariable String batchId) {

        return ResponseEntity.ok(
                service.getCourseByBatchId(batchId)
        );
    }


    // =========================================================
    // UPDATE COURSE
    // ADMIN + TEAM LEAD
    // =========================================================

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateCourse(

            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader,

            @PathVariable Long id,

            @RequestBody Course course) {

        String creatorName =
                getAuthorizedCreatorName(authHeader);

        if (creatorName == null) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "Access Denied: Only Admin or Team Lead allowed to modify courses"
                    );
        }

        return ResponseEntity.ok(
                service.updateCourse(id, course)
        );
    }


    // =========================================================
    // DELETE COURSE
    // ADMIN + TEAM LEAD
    // =========================================================

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteCourse(

            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader,

            @PathVariable Long id) {

        String creatorName =
                getAuthorizedCreatorName(authHeader);

        if (creatorName == null) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "Access Denied: Only Admin or Team Lead allowed to delete courses"
                    );
        }

        service.deleteCourse(id);

        return ResponseEntity.ok(
                "Course Deleted Successfully"
        );
    }


    // =========================================================
    // UPDATE COURSE STATUS
    // ADMIN + TEAM LEAD
    // =========================================================

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> toggleCourseStatus(

            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader,

            @PathVariable Long id,

            @RequestParam String status) {

        String creatorName =
                getAuthorizedCreatorName(authHeader);

        if (creatorName == null) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "Access Denied: Only Admin or Team Lead allowed to modify courses"
                    );
        }

        return ResponseEntity.ok(
                service.updateStatus(id, status)
        );
    }
}