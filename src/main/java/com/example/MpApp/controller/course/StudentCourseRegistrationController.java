package com.example.MpApp.controller.course;

import com.example.MpApp.dto.course.StudentCourseRegistrationRequest;
import com.example.MpApp.entity.course.StudentCourseRegistration;
import com.example.MpApp.service.course.StudentCourseRegistrationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-course")
public class StudentCourseRegistrationController {

    @Autowired
    private StudentCourseRegistrationService service;

    /*
    ===================================
    REGISTER COURSE
    ===================================
    */

    @PostMapping("/register")
    public ResponseEntity<?> registerCourse(

            @RequestHeader(value = "Authorization",
                    required = false)
            String authHeader,

            @RequestBody
            StudentCourseRegistrationRequest request) {

        try{
            if (authHeader == null ||
                    !authHeader.startsWith("Bearer ")) {

                return ResponseEntity
                        .status(403)
                        .body("Token Required");
            }

            String token =
                    authHeader.substring(7);
            service.registerCourse(
                    token,
                    request);
            return ResponseEntity.ok(
                    "register successfully"
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
    ===================================
    GET ALL REGISTRATIONS
    ===================================
    */

    @GetMapping("/get-all")
    public ResponseEntity<List<StudentCourseRegistration>>
    getAllRegistrations() {

        return ResponseEntity.ok(
                service.getAllRegistrations());
    }

    /*
    ===================================
    GET REGISTRATION BY ID
    ===================================
    */

    @GetMapping("/get/{id}")
    public ResponseEntity<StudentCourseRegistration>
    getRegistrationById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.getRegistrationById(id));
    }

    /*
    ===================================
    GET ALL COURSES OF STUDENT
    ===================================
    */

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentCourseRegistration>>
    getByStudentId(
            @PathVariable String studentId) {

        return ResponseEntity.ok(
                service.getByStudentId(
                        studentId));
    }

    /*
    ===================================
    DELETE REGISTRATION
    ===================================
    */

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String>
    deleteRegistration(
            @PathVariable Long id) {

        service.deleteRegistration(id);

        return ResponseEntity.ok(
                "Registration Deleted Successfully");
    }

    @GetMapping("/my-courses")
    public ResponseEntity<?> myCourses(

            @RequestHeader("Authorization")
            String authHeader) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            return ResponseEntity
                    .status(403)
                    .body("Token Required");
        }

        String token =
                authHeader.substring(7);

        return ResponseEntity.ok(
                service.getMyRegistrations(
                        token));
    }
}