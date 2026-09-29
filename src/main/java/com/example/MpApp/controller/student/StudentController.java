package com.example.MpApp.controller.student;

import com.example.MpApp.dto.common.ChangePasswordRequest;
import com.example.MpApp.dto.common.ForgotPasswordRequest;
import com.example.MpApp.dto.common.OtpRequest;
import com.example.MpApp.dto.common.VerifyOtpRequest;
import com.example.MpApp.dto.file.FileViewResponse;
import com.example.MpApp.dto.student.StudentLoginRequest;
import com.example.MpApp.dto.student.StudentRegisterRequest;
import com.example.MpApp.entity.student.Notification;
import com.example.MpApp.entity.student.Student;
import com.example.MpApp.service.student.NotificationService;
import com.example.MpApp.service.student.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@Validated
public class StudentController {

    @Autowired
    private StudentService service;

    @Autowired
    private NotificationService notificationService;

    // ================= REGISTER =================
    @PostMapping("/register")
    public ResponseEntity<?> registerStudent(
            @Valid @RequestBody StudentRegisterRequest request) {

        return ResponseEntity.ok(service.registerStudent(request));
    }

    @PutMapping("/update-files")
    public ResponseEntity<?> updateStudentFiles(
            Authentication authentication,
            @RequestParam(value = "profile", required = false) MultipartFile profile) {
        return ResponseEntity.ok(service.updateStudentFilesByEmail(authentication.getName(), profile));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(Authentication authentication) {
        return ResponseEntity.ok(service.getStudentDashboardByEmail(authentication.getName()));
    }

    // 📥 Retrieval Endpoint
    @GetMapping("/files")
    public ResponseEntity<FileViewResponse> getStudentFiles(Authentication authentication) {
        return ResponseEntity.ok(service.getStudentFilesByEmail(authentication.getName()));
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public Map<String, String> loginStudent(
            @Valid @RequestBody StudentLoginRequest request) {
        return service.loginStudent(request);
    }


    // ================= SEND OTP =================
    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody OtpRequest request) {
        return ResponseEntity.ok(Map.of("message", service.sendOtp(request.getEmail())));
    }

    // ================= RESEND OTP =================
    @PostMapping("/forgot-password/resend-otp")
    public ResponseEntity<?> resendOtp(@Valid @RequestBody OtpRequest request) {
        return ResponseEntity.ok(Map.of("message", service.sendOtp(request.getEmail())));
    }

    // ================= VERIFY OTP =================
    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        service.verifyOtp(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(Map.of("message", "OTP Verified Successfully"));
    }

    // ================= RESET PASSWORD =================
    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(
                Map.of("message", service.resetPassword(
                        request.getEmail(),
                        request.getOtp(),
                        request.getNewPassword()
                ))
        );
    }

    // ================= UPDATE PROFILE =================
    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(
            Authentication authentication,
            @RequestBody Student student) {
        return ResponseEntity.ok(service.updateProfileByEmail(authentication.getName(), student));
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getNotifications(Authentication authentication) {
        return ResponseEntity.ok(notificationService.getNotificationsByStudentEmail(authentication.getName()));
    }

    @PatchMapping("/notifications/{notificationId}/read")
    public ResponseEntity<?> markAsRead(
            Authentication authentication,
            @PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId, authentication.getName());
        return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
    }

    // Inside StudentController.java

    @PatchMapping("/change-password")
    public ResponseEntity<?> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {

        String oldPassword = request.getOldPassword();
        String newPassword = request.getNewPassword();

        String message = service.changePasswordByEmail(authentication.getName(), oldPassword, newPassword);

        return ResponseEntity.ok(Map.of("message", message));
    }
}