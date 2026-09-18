package com.example.MpApp.controller.course;

import com.example.MpApp.dto.course.StudentCourseRegistrationRequest;
import com.example.MpApp.entity.course.StudentCourseRegistration;


import com.example.MpApp.service.course.InternshipRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-internship")
@CrossOrigin("*")
public class InternshipRegistrationController {

    @Autowired
    private InternshipRegistrationService service;


    // =========================================================
    // REGISTER INTERNSHIP
    // =========================================================
    /*
     * Student registers for an internship.
     *
     * Student sends only:
     *
     * courseId  (the internship's id in the `courses` table)
     * mode
     * location
     *
     * Student personal information comes from
     * the existing Student account, same as
     * course registration.
     */

    @PostMapping("/register")
    public ResponseEntity<?> registerInternship(

            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader,

            @RequestBody
            StudentCourseRegistrationRequest request) {

        // -----------------------------------------------------
        // TOKEN VALIDATION
        // -----------------------------------------------------

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Token Required");
        }


        // -----------------------------------------------------
        // EXTRACT TOKEN
        // -----------------------------------------------------

        String token =
                authHeader.substring(7);


        // -----------------------------------------------------
        // REGISTER INTERNSHIP
        // -----------------------------------------------------

        return ResponseEntity.ok(
                service.registerInternship(
                        token,
                        request
                )
        );
    }


    // =========================================================
    // GET ALL INTERNSHIP REGISTRATIONS
    // =========================================================
    /*
     * Used by admin/staff side to view
     * all internship registrations.
     */

    @GetMapping("/get-all")
    public ResponseEntity<List<StudentCourseRegistration>>getAllRegistrations() {

        return ResponseEntity.ok(
                service.getAllRegistrations()
        );
    }


    // =========================================================
    // GET REGISTRATION BY ID
    // =========================================================

    @GetMapping("/get/{id}")
    public ResponseEntity<StudentCourseRegistration>getRegistrationById(

            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.getRegistrationById(id)
        );
    }


    // =========================================================
    // GET STUDENT'S INTERNSHIP REGISTRATIONS
    // =========================================================

    @GetMapping("/student/{studentId}")
    public ResponseEntity <List<StudentCourseRegistration>>getByStudentId(

            @PathVariable String studentId) {

        return ResponseEntity.ok(
                service.getByStudentId(
                        studentId
                )
        );
    }


    // =========================================================
    // GET INTERNSHIP'S REGISTRATIONS
    // =========================================================
    /*
     * internshipId is the same as the
     * course id in the `courses` table
     * (category = INTERNSHIP).
     */

    @GetMapping("/internship/{internshipId}")
    public ResponseEntity <List<StudentCourseRegistration>>getByInternshipId(

            @PathVariable Long internshipId) {

        return ResponseEntity.ok(
                service.getByInternshipId(
                        internshipId
                )
        );
    }


    // =========================================================
    // UPDATE INTERNSHIP REGISTRATION MODE / LOCATION
    // ONLY BEFORE PAYMENT
    // =========================================================

    @PatchMapping("/update/{registrationId}")
    public ResponseEntity<?> updateInternshipRegistration(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader,

            @PathVariable Long registrationId,

            @RequestBody StudentCourseRegistrationRequest request) {

        // -----------------------------------------------------
        // TOKEN VALIDATION
        // -----------------------------------------------------

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Token Required");
        }

        String token = authHeader.substring(7);

        // -----------------------------------------------------
        // UPDATE MODE / LOCATION
        // -----------------------------------------------------

        return ResponseEntity.ok(
                service.updateRegistrationMode(
                        token,
                        registrationId,
                        request
                )
        );
    }


    // =========================================================
    // DELETE REGISTRATION
    // =========================================================

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String>
    deleteRegistration(

            @PathVariable Long id) {

        service.deleteRegistration(id);

        return ResponseEntity.ok(
                "Registration Deleted Successfully"
        );
    }


    // =========================================================
    // MY INTERNSHIPS
    // =========================================================
    /*
     * Gets internships registered by the
     * currently logged-in student.
     *
     * Student is identified from JWT.
     */

    @GetMapping("/my-internships")
    public ResponseEntity<?> myInternships(

            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader) {


        // -----------------------------------------------------
        // TOKEN VALIDATION
        // -----------------------------------------------------

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Token Required");
        }


        // -----------------------------------------------------
        // EXTRACT TOKEN
        // -----------------------------------------------------

        String token =
                authHeader.substring(7);


        // -----------------------------------------------------
        // GET STUDENT INTERNSHIPS
        // -----------------------------------------------------

        return ResponseEntity.ok(
                service.getMyRegistrations(
                        token
                )
        );
    }
}