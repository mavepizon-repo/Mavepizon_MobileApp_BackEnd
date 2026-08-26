package com.example.MpApp.controller.officestaff;

import com.example.MpApp.dto.common.ForgotPasswordRequest;
import com.example.MpApp.dto.file.FileViewResponse;
import com.example.MpApp.dto.officestaff.*;
import com.example.MpApp.entity.officestaff.OfficeStaff;
import com.example.MpApp.entity.officestaff.OfficeStaffAttendance;
import com.example.MpApp.entity.officestaff.OfficeStaffPermission;
import com.example.MpApp.service.officestaff.OfficeStaffAttendanceService;
import com.example.MpApp.service.officestaff.OfficeStaffService;
import com.example.MpApp.dto.task.TaskResponse;
import com.example.MpApp.dto.task.TaskUpdateRequest;
import com.example.MpApp.entity.task.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/officestaff")
@RequiredArgsConstructor
public class OfficeStaffController {

    private final OfficeStaffService service;
    private final OfficeStaffAttendanceService attendanceService;

    // LOGIN ONLY
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody OfficeStaffLoginRequest request) {
        return service.loginOfficeStaff(request);
    }

    // TASKS
    @GetMapping("/{staffId}/tasks")
    public ResponseEntity<List<TaskResponse>> myTasks(@PathVariable Long staffId) {
        return ResponseEntity.ok(service.getMyTasks(staffId));
    }

    @PutMapping("/task/{taskId}/progress")
    public ResponseEntity<Task> updateProgress(
            @PathVariable Long taskId,
            @RequestBody TaskUpdateRequest request) {
        return ResponseEntity.ok(service.updateProgress(taskId, request));
    }

    @PutMapping("/task/{taskId}/submit")
    public ResponseEntity<Task> submitTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(service.submitTask(taskId));
    }

    @GetMapping("/{staffId}/profile")
    public ResponseEntity<OfficeStaffProfileResponse>
    getProfile(
            @PathVariable Long staffId)
    {
        return ResponseEntity.ok(
                service.getProfile(staffId));
    }

    @PostMapping("/{staffId}/leave")
    public ResponseEntity<?> requestLeave(@PathVariable Long staffId, @RequestBody LeaveRequestDTO request) {
        return ResponseEntity.ok(service.requestLeave(staffId, request));
    }

    @GetMapping("/{staffId}/leave-history")public ResponseEntity<?> getMyLeaveHistory(@PathVariable Long staffId) {
        return ResponseEntity.ok(service.getLeaveHistory(staffId));
    }

    /*
    ===================================
    REQUEST PERMISSION (1 OR 2 HOURS)
    ===================================
    */
    @PostMapping("/{staffId}/permissions")
    public ResponseEntity<Map<String, String>> requestPermission(
            @PathVariable Long staffId,
            @RequestBody PermissionRequestDTO requestDTO) {

        return ResponseEntity.ok(service.requestPermission(staffId, requestDTO));
    }

    /*
    ===================================
    GET PERMISSION HISTORY
    ===================================
    */
    @GetMapping("/{staffId}/permissions/history")
    public ResponseEntity<List<OfficeStaffPermission>> getPermissionHistory(
            @PathVariable Long staffId) {

        return ResponseEntity.ok(service.getPermissionHistory(staffId));
    }

    @PostMapping("/{staffId}/checkin")
    public ResponseEntity<OfficeStaffAttendance> checkIn(@PathVariable Long staffId, @RequestBody CheckInRequestDTO checkInRequestDTO) {
        return ResponseEntity.ok(attendanceService.checkIn(staffId,checkInRequestDTO));
    }

    @PostMapping("/{staffId}/checkout")
    public ResponseEntity<OfficeStaffAttendance> checkOut(@PathVariable Long staffId) {
        return ResponseEntity.ok(attendanceService.checkOut(staffId));
    }

    @GetMapping("/{staffId}/history")
    public ResponseEntity<List<OfficeStaffAttendance>> getHistory(@PathVariable Long staffId) {
        return ResponseEntity.ok(attendanceService.getStaffAttendanceHistory(staffId));
    }

    // ================= FORGOT & RESET PASSWORD =================
    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam String email) {
        return ResponseEntity.ok(service.sendOtp(email));
    }

    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        return ResponseEntity.ok(service.verifyOtp(email, otp));
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPassword(
            @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(service.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword()));
    }

    @GetMapping("/files/{id}")
    public ResponseEntity<FileViewResponse> getStaffFiles(@PathVariable Long id) {
        return ResponseEntity.ok(service.getStaffFiles(id));
    }

    @GetMapping("/{staffId}/performance-summary")
    public ResponseEntity<PerformanceSummaryDTO> getPerformanceSummary(@PathVariable Long staffId) {
        return ResponseEntity.ok(service.getStaffPerformanceSummary(staffId));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<OfficeStaff>> getLeaderboard() {
        return ResponseEntity.ok(service.getLeaderboard());
    }

    // Add this to OfficeStaffController.java

    @PatchMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        // The service method will throw exceptions (IllegalStateException, InvalidCredentialsException)
        // if the logic fails, which the GlobalExceptionHandler will automatically handle.
        String message = service.changePassword(email, oldPassword, newPassword);

        return ResponseEntity.ok(Map.of("message", message));
    }
}