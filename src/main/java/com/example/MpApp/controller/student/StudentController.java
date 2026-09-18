package com.example.MpApp.controller.student;

import com.example.MpApp.dto.file.FileViewResponse;
import com.example.MpApp.dto.student.StudentLoginRequest;
import com.example.MpApp.dto.student.StudentRegisterRequest;
import com.example.MpApp.entity.student.Notification;
import com.example.MpApp.entity.student.Student;
import com.example.MpApp.service.student.NotificationService;
import com.example.MpApp.service.student.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private StudentService service;

    @Autowired
    private NotificationService notificationService;

    // ================= REGISTER =================
    @PostMapping("/register")
    public ResponseEntity<?> registerStudent(
            @RequestBody StudentRegisterRequest request) {

        return ResponseEntity.ok(service.registerStudent(request));
    }

    @PutMapping("/update-files/{id}")
    public ResponseEntity<?> updateStudentFiles(
            @PathVariable Long id,
            @RequestParam(value = "profile", required = false) MultipartFile profile) {
        return ResponseEntity.ok(service.updateStudentFiles(id, profile));
    }

    @GetMapping("/{studentId}/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(@PathVariable Long studentId) {
        return ResponseEntity.ok(service.getStudentDashboard(studentId));
    }

    // 📥 Retrieval Endpoint
    @GetMapping("/files/{id}")
    public ResponseEntity<FileViewResponse> getStudentFiles(@PathVariable Long id) {
        return ResponseEntity.ok(service.getStudentFiles(id));
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

    @GetMapping("/{studentId}/notifications")
    public ResponseEntity<List<Notification>> getNotifications(@PathVariable Long studentId) {
        return ResponseEntity.ok(notificationService.getNotificationsByStudent(studentId));
    }

    @PatchMapping("/notifications/{notificationId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
    }

    // Inside StudentController.java

    @PatchMapping("/{studentId}/change-password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long studentId,
            @RequestBody Map<String, String> request) {

        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        String message = service.changePassword(studentId, oldPassword, newPassword);

        return ResponseEntity.ok(Map.of("message", message));
    }
}