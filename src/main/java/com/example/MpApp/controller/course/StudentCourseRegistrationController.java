package com.example.MpApp.controller.course;

import com.example.MpApp.dto.course.StudentCourseRegistrationRequest;
import com.example.MpApp.entity.course.StudentCourseRegistration;
import com.example.MpApp.service.course.StudentCourseRegistrationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-course")
@CrossOrigin("*")
public class StudentCourseRegistrationController {

    @Autowired
    private StudentCourseRegistrationService service;


    // =========================================================
    // REGISTER COURSE
    // =========================================================
    /*
     * Student registers for a course.
     *
     * Student sends only:
     *
     * courseId
     * mode
     * location
     *
     * Student personal information such as:
     * name, email, phone, DOB, gender, address,
     * profile image, etc. should come from
     * the existing Student account.
     */

    @PostMapping("/register")
    public ResponseEntity<?> registerCourse(

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
        // REGISTER COURSE
        // -----------------------------------------------------

        return ResponseEntity.ok(
                service.registerCourse(
                        token,
                        request
                )
        );
    }


    // =========================================================
    // GET ALL REGISTRATIONS
    // =========================================================
    /*
     * Used by admin/staff side to view
     * all course registrations.
     */

    @GetMapping("/get-all")
    public ResponseEntity<
            List<StudentCourseRegistration>>
    getAllRegistrations() {

        return ResponseEntity.ok(
                service.getAllRegistrations()
        );
    }


    // =========================================================
    // GET REGISTRATION BY ID
    // =========================================================

    @GetMapping("/get/{id}")
    public ResponseEntity<
            StudentCourseRegistration>
    getRegistrationById(

            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.getRegistrationById(id)
        );
    }


    // =========================================================
    // GET STUDENT REGISTRATIONS
    // =========================================================
    /*
     * Returns all courses registered by
     * a particular student.
     */

    @GetMapping("/student/{studentId}")
    public ResponseEntity<
            List<StudentCourseRegistration>>
    getByStudentId(

            @PathVariable String studentId) {

        return ResponseEntity.ok(
                service.getByStudentId(
                        studentId
                )
        );
    }


    // =========================================================
    // GET COURSE REGISTRATIONS
    // =========================================================
    /*
     * Returns all students registered
     * for a particular course.
     */

    @GetMapping("/course/{courseId}")
    public ResponseEntity<
            List<StudentCourseRegistration>>
    getByCourseId(

            @PathVariable Long courseId) {

        return ResponseEntity.ok(
                service.getByCourseId(
                        courseId
                )
        );
    }


    // =========================================================
// UPDATE COURSE REGISTRATION MODE / LOCATION
// ONLY BEFORE PAYMENT
// =========================================================

    @PatchMapping("/update/{registrationId}")
    public ResponseEntity<?> updateCourseRegistration(
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
    // MY COURSES
    // =========================================================
    /*
     * Gets courses registered by the
     * currently logged-in student.
     *
     * Student is identified from JWT.
     */

    @GetMapping("/my-courses")
    public ResponseEntity<?> myCourses(

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
        // GET STUDENT COURSES
        // -----------------------------------------------------

        return ResponseEntity.ok(
                service.getMyRegistrations(
                        token
                )
        );
    }
}