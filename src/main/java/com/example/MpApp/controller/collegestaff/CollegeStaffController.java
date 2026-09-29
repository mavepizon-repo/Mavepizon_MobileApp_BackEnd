package com.example.MpApp.controller.collegestaff;

import com.example.MpApp.dto.collegestaff.CollegeStaffLoginRequest;
import com.example.MpApp.dto.common.ChangePasswordRequest;
import com.example.MpApp.dto.common.ForgotPasswordRequest;
import com.example.MpApp.dto.common.OtpRequest;
import com.example.MpApp.dto.common.VerifyOtpRequest;
import com.example.MpApp.service.collegestaff.CollegeStaffService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/collegestaff")
@Validated
public class CollegeStaffController {

    @Autowired
    private CollegeStaffService service;

    @PostMapping("/login")
    public Map<String, String> loginCollegeStaff(
            @Valid @RequestBody CollegeStaffLoginRequest request) {
        return service.loginCollegeStaff(request);
    }

    @GetMapping("/myfiles")
    public ResponseEntity<?> getStaffFiles(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(service.getAllFiles(authHeader));
    }

    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody OtpRequest request) {
        return ResponseEntity.ok(Map.of("message", service.sendOtp(request.getEmail())));
    }

    @PostMapping("/forgot-password/resend-otp")
    public ResponseEntity<?> resendOtp(@Valid @RequestBody OtpRequest request) {
        return ResponseEntity.ok(Map.of("message", service.sendOtp(request.getEmail())));
    }

    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        service.verifyOtp(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(Map.of("message", "OTP Verified Successfully"));
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(Map.of(
                "message",
                service.resetPassword(
                        request.getEmail(),
                        request.getOtp(),
                        request.getNewPassword()
                )
        ));
    }

    @PatchMapping("/change-password")
    public ResponseEntity<?> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(Map.of(
                "message",
                service.changePassword(
                        authentication.getName(),
                        request.getOldPassword(),
                        request.getNewPassword()
                )
        ));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        return ResponseEntity.ok(service.getProfileByEmail(authentication.getName()));
    }

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
