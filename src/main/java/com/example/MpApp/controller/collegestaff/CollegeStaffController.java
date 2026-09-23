package com.example.MpApp.controller.collegestaff;

import com.example.MpApp.dto.collegestaff.CollegeStaffLoginRequest;
import com.example.MpApp.dto.common.ForgotPasswordRequest;
import com.example.MpApp.service.collegestaff.CollegeStaffService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/collegestaff")
@CrossOrigin("*")
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
    public ResponseEntity<?> sendOtp(@RequestParam @NotBlank @Email String email) {
        return ResponseEntity.ok(Map.of("message", service.sendOtp(email)));
    }

    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam @NotBlank @Email String email, @RequestParam @NotBlank @Pattern(regexp = "^\\d{6}$") String otp) {
        return ResponseEntity.ok(Map.of("message", service.verifyOtp(email, otp)));
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

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            Authentication authentication,
            @RequestParam String oldPassword,
            @RequestParam @NotBlank @Size(min = 8) String newPassword) {
        return ResponseEntity.ok(Map.of(
                "message",
                service.changePassword(
                        authentication.getName(),
                        oldPassword,
                        newPassword
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
