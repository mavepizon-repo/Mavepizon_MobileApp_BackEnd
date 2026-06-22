package com.example.MpApp.controller.student;

import com.example.MpApp.dto.student.StudentLoginRequest;
import com.example.MpApp.dto.student.StudentRegisterRequest;
import com.example.MpApp.entity.student.Student;
import com.example.MpApp.service.student.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private StudentService service;

    // ================= REGISTER =================
    @PostMapping("/register")
    public ResponseEntity<?> registerStudent(
            @RequestBody StudentRegisterRequest request) {
        return ResponseEntity.ok(service.registerStudent(request));
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public Map<String, String> loginStudent(
            @RequestBody StudentLoginRequest request) {
        return service.loginStudent(request);
    }


    // ================= SEND OTP =================
    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<?> sendOtp(
            @RequestParam String email) {
        return ResponseEntity.ok(service.sendOtp(email));
    }

    // ================= VERIFY OTP =================
    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @RequestParam String email,
            @RequestParam String otp) {
        return ResponseEntity.ok(service.verifyOtp(email, otp));
    }

    // ================= RESET PASSWORD =================
    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPassword(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword) {
        return ResponseEntity.ok(service.resetPassword(email, otp, newPassword));
    }

    // ================= UPDATE PROFILE =================
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateProfile(
            @PathVariable Long id,
            @RequestBody Student student) {
        return ResponseEntity.ok(service.updateProfile(id, student));
    }
}