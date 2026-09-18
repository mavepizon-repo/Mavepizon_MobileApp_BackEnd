package com.example.MpApp.controller.course;

import com.example.MpApp.config.JwtService;
import com.example.MpApp.dto.course.CourseStaffAssignmentRequest;
import com.example.MpApp.entity.admin.Admin;
import com.example.MpApp.entity.course.CourseStaffAssignment;
import com.example.MpApp.entity.teamlead.TeamLead;
import com.example.MpApp.repository.admin.AdminRepository;
import com.example.MpApp.repository.teamlead.TeamLeadRepository;
import com.example.MpApp.service.course.CourseStaffAssignmentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course-staff-assignment")
@CrossOrigin("*")
public class CourseStaffAssignmentController {

    @Autowired
    private CourseStaffAssignmentService service;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private TeamLeadRepository teamLeadRepository;


    // =========================================================
    // VALIDATE ADMIN / TEAM LEAD
    // =========================================================

    private String validateAdminOrTeamLead(
            String authHeader) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            throw new RuntimeException(
                    "Token Required"
            );
        }

        String token =
                authHeader.substring(7);

        String email;

        try {

            email =
                    jwtService.extractUsername(token);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Invalid or expired token"
            );
        }

        if (email == null ||
                email.isBlank()) {

            throw new RuntimeException(
                    "Invalid token"
            );
        }


        // =====================================================
        // CHECK ADMIN
        // =====================================================

        Admin admin =
                adminRepository
                        .findByEmail(email)
                        .orElse(null);

        if (admin != null) {
            return email;
        }


        // =====================================================
        // CHECK TEAM LEAD
        // =====================================================

        TeamLead teamLead =
                teamLeadRepository
                        .findByEmail(email)
                        .orElse(null);

        if (teamLead != null) {
            return email;
        }


        throw new RuntimeException(
                "Access Denied: Only Admin or Team Lead can assign staff"
        );
    }


    // =========================================================
    // CREATE STAFF ASSIGNMENT
    // ADMIN / TEAM LEAD
    // =========================================================

    @PostMapping("/course/{courseId}")
    public ResponseEntity<?> createAssignment(

            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader,

            @PathVariable Long courseId,

            @RequestBody
            CourseStaffAssignmentRequest request) {

        try {

            String email =
                    validateAdminOrTeamLead(
                            authHeader
                    );

            CourseStaffAssignment assignment =
                    service.createAssignment(
                            courseId,
                            request,
                            email
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(assignment);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }


    // =========================================================
    // UPDATE STAFF ASSIGNMENT
    // ADMIN / TEAM LEAD
    // =========================================================

    @PutMapping("/course/{courseId}")
    public ResponseEntity<?> updateAssignment(

            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader,

            @PathVariable Long courseId,

            @RequestBody
            CourseStaffAssignmentRequest request) {

        try {

            String email =
                    validateAdminOrTeamLead(
                            authHeader
                    );

            CourseStaffAssignment assignment =
                    service.updateAssignment(
                            courseId,
                            request,
                            email
                    );

            return ResponseEntity.ok(
                    assignment
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }


    // =========================================================
    // GET STAFF ASSIGNMENT FOR COURSE
    // =========================================================

    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getAssignmentByCourse(

            @PathVariable Long courseId) {

        try {

            return ResponseEntity.ok(
                    service.getAssignmentByCourse(
                            courseId
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }


    // =========================================================
    // GET ALL STAFF ASSIGNMENTS
    // =========================================================

    @GetMapping("/get-all")
    public ResponseEntity<
            List<CourseStaffAssignment>>
    getAllAssignments() {

        return ResponseEntity.ok(
                service.getAllAssignments()
        );
    }


    // =========================================================
    // DELETE STAFF ASSIGNMENT
    // ADMIN / TEAM LEAD
    // =========================================================

    @DeleteMapping("/course/{courseId}")
    public ResponseEntity<?> deleteAssignment(

            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader,

            @PathVariable Long courseId) {

        try {

            validateAdminOrTeamLead(
                    authHeader
            );

            service.deleteAssignment(
                    courseId
            );

            return ResponseEntity.ok(
                    "Course Staff Assignment Deleted Successfully"
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}