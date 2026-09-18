package com.example.MpApp.controller.course;

import com.example.MpApp.config.JwtService;
import com.example.MpApp.entity.admin.Admin;
import com.example.MpApp.entity.course.Course;
import com.example.MpApp.entity.teamlead.TeamLead;
import com.example.MpApp.repository.admin.AdminRepository;
import com.example.MpApp.repository.teamlead.TeamLeadRepository;


import com.example.MpApp.service.course.InternshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/internship")
@CrossOrigin("*")
public class InternshipController {

    @Autowired
    private InternshipService service;

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
    // CREATE INTERNSHIP
    // ADMIN + TEAM LEAD
    // =========================================================

    @PostMapping("/create")
    public ResponseEntity<?> createInternship(

            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader,

            @RequestBody Course internship) {

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
        internship.setCreatedBy(creatorName);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        service.createInternship(internship)
                );
    }


    // =========================================================
    // GET ALL INTERNSHIPS
    // =========================================================

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllInternships() {

        return ResponseEntity.ok(
                service.getAllInternships()
        );
    }


    // =========================================================
    // GET INTERNSHIP BY DATABASE ID
    // =========================================================

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getInternshipById(

            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.getInternshipById(id)
        );
    }


    // =========================================================
    // GET INTERNSHIP BY INTERNSHIP CODE
    // =========================================================

    @GetMapping("/get/code/{internshipCode}")
    public ResponseEntity<?> getInternshipByCode(

            @PathVariable String internshipCode) {

        return ResponseEntity.ok(
                service.getInternshipByCode(internshipCode)
        );
    }


    // =========================================================
    // GET INTERNSHIP BY BATCH ID
    // =========================================================

    @GetMapping("/get/batch/{batchId}")
    public ResponseEntity<?> getInternshipByBatchId(

            @PathVariable String batchId) {

        return ResponseEntity.ok(
                service.getInternshipByBatchId(batchId)
        );
    }


    // =========================================================
    // UPDATE INTERNSHIP
    // ADMIN + TEAM LEAD
    // =========================================================

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateInternship(

            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader,

            @PathVariable Long id,

            @RequestBody Course internship) {

        String creatorName =
                getAuthorizedCreatorName(authHeader);

        if (creatorName == null) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "Access Denied: Only Admin or Team Lead allowed to modify internships"
                    );
        }

        return ResponseEntity.ok(
                service.updateInternship(id, internship)
        );
    }


    // =========================================================
    // DELETE INTERNSHIP
    // ADMIN + TEAM LEAD
    // =========================================================

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteInternship(

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
                            "Access Denied: Only Admin or Team Lead allowed to delete internships"
                    );
        }

        service.deleteInternship(id);

        return ResponseEntity.ok(
                "Internship Deleted Successfully"
        );
    }


    // =========================================================
    // UPDATE INTERNSHIP STATUS
    // ADMIN + TEAM LEAD
    // =========================================================

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> toggleInternshipStatus(

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
                            "Access Denied: Only Admin or Team Lead allowed to modify internships"
                    );
        }

        return ResponseEntity.ok(
                service.updateStatus(id, status)
        );
    }
}