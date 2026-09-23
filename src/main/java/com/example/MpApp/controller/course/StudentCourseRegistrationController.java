package com.example.MpApp.controller.course;

import com.example.MpApp.dto.course.StudentCourseRegistrationRequest;
import com.example.MpApp.entity.course.StudentCourseRegistration;
import com.example.MpApp.service.course.StudentCourseRegistrationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student-course")
@CrossOrigin("*")
public class StudentCourseRegistrationController {

    @Autowired
    private StudentCourseRegistrationService service;

    // =========================================================
    // REGISTER COURSE
    // =========================================================

    @PostMapping("/register")
    public ResponseEntity<?> registerCourse(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader,
            @Valid @RequestBody
            StudentCourseRegistrationRequest request) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            return tokenRequiredError();
        }

        String token = authHeader.substring(7);

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

    @GetMapping("/get-all")
    public ResponseEntity<List<StudentCourseRegistration>>
    getAllRegistrations() {

        return ResponseEntity.ok(
                service.getAllRegistrations()
        );
    }

    // =========================================================
    // GET REGISTRATION BY ID
    // =========================================================

    @GetMapping("/get/{id}")
    public ResponseEntity<StudentCourseRegistration>
    getRegistrationById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.getRegistrationById(id)
        );
    }

    // =========================================================
    // GET STUDENT REGISTRATIONS
    // =========================================================

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentCourseRegistration>>
    getByStudentId(
            @PathVariable String studentId) {

        return ResponseEntity.ok(
                service.getByStudentId(studentId)
        );
    }

    // =========================================================
    // GET COURSE REGISTRATIONS
    // =========================================================

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<StudentCourseRegistration>>
    getByCourseId(
            @PathVariable Long courseId) {

        return ResponseEntity.ok(
                service.getByCourseId(courseId)
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
            @Valid @RequestBody
            StudentCourseRegistrationRequest request) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            return tokenRequiredError();
        }

        String token = authHeader.substring(7);

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
    public ResponseEntity<Map<String, Object>>
    deleteRegistration(
            @PathVariable Long id) {

        service.deleteRegistration(id);

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("success", true);
        response.put(
                "message",
                "Registration Deleted Successfully"
        );
        response.put(
                "timestamp",
                Instant.now().toString()
        );

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // MY COURSES
    // =========================================================

    @GetMapping("/my-courses")
    public ResponseEntity<?> myCourses(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            return tokenRequiredError();
        }

        String token = authHeader.substring(7);

        return ResponseEntity.ok(
                service.getMyRegistrations(token)
        );
    }

    // =========================================================
    // CERTIFICATE ELIGIBLE STUDENTS
    // =========================================================

    @GetMapping("/certificate-eligible")
    public ResponseEntity<List<StudentCourseRegistration>>
    getCertificateEligibleStudents() {

        return ResponseEntity.ok(
                service.getCertificateEligibleStudents()
        );
    }

    // =========================================================
    // STANDARD TOKEN ERROR
    // =========================================================

    private ResponseEntity<Map<String, Object>>
    tokenRequiredError() {

        Map<String, Object> error =
                new LinkedHashMap<>();

        error.put("success", false);
        error.put("errorCode", "UNAUTHORIZED");
        error.put(
                "timestamp",
                Instant.now().toString()
        );
        error.put(
                "status",
                HttpStatus.UNAUTHORIZED.value()
        );
        error.put(
                "error",
                "Unauthorized"
        );
        error.put(
                "message",
                "Token Required"
        );
        error.put(
                "path",
                "/api/student-course"
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(error);
    }
}