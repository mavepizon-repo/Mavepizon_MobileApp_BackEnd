package com.example.MpApp.controller.collegestaff;

import com.example.MpApp.dto.collegestaff.CollegeStaffLoginRequest;
import com.example.MpApp.dto.common.ForgotPasswordRequest;
import com.example.MpApp.entity.collegestaff.CollegeStaff;
import com.example.MpApp.service.collegestaff.CollegeStaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/collegestaff")
@CrossOrigin("*")
public class CollegeStaffController {

    @Autowired
    private CollegeStaffService service;

    @PostMapping("/login")
    public Map<String, String> loginCollegeStaff(
            @RequestBody CollegeStaffLoginRequest request) {
        return service.loginCollegeStaff(request);
    }

    @GetMapping("/myfiles")
    public ResponseEntity<?> getStaffFiles(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(
                service.getAllFiles(authHeader)
        );
    }



    // ================= FORGOT PASSWORD FLOW =================

    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam String email) {
        return ResponseEntity.ok(Map.of("message", service.sendOtp(email)));
    }

    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        return ResponseEntity.ok(Map.of("message", service.verifyOtp(email, otp)));
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPassword(
            @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(Map.of("message", service.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword())));
    }

    // ================= CHANGE PASSWORD FLOW =================

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestParam String email,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        return ResponseEntity.ok(Map.of("message", service.changePassword(email, oldPassword, newPassword)));
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<CollegeStaff> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(service.getProfile(id));
    }

    // ================= STUDENT UPLOAD =================

    @PostMapping("/upload-students")
    public ResponseEntity<?> uploadStudents(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("file") MultipartFile file) {

        try {
            Map<String, Object> response = service.uploadStudentExcel(authHeader, file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}