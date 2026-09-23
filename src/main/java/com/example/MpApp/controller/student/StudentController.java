package com.example.MpApp.controller.student;

import com.example.MpApp.dto.common.ChangePasswordRequest;
import com.example.MpApp.dto.file.FileViewResponse;
import com.example.MpApp.dto.student.StudentLoginRequest;
import com.example.MpApp.dto.student.StudentRegisterRequest;
import com.example.MpApp.entity.student.Notification;
import com.example.MpApp.entity.student.Student;
import com.example.MpApp.service.student.NotificationService;
import com.example.MpApp.service.student.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    public ResponseEntity<?> sendOtp(
            @RequestParam @NotBlank @Email String email) {
        return ResponseEntity.ok(service.sendOtp(email));
    }

    // ================= VERIFY OTP =================
    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @RequestParam @NotBlank @Email String email,
            @RequestParam @NotBlank @Pattern(regexp = "^\\d{6}$") String otp) {
        return ResponseEntity.ok(service.verifyOtp(email, otp));
    }

    // ================= RESET PASSWORD =================
    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPassword(
            @RequestParam @NotBlank @Email String email,
            @RequestParam @NotBlank @Pattern(regexp = "^\\d{6}$") String otp,
            @RequestParam @NotBlank @Size(min = 8) String newPassword) {
        return ResponseEntity.ok(service.resetPassword(email, otp, newPassword));
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