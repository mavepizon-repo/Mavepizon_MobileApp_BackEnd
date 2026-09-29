package com.example.MpApp.controller.officestaff;

import com.example.MpApp.dto.Attendance.AttendanceResponseDTO;
import com.example.MpApp.dto.common.ChangePasswordRequest;
import com.example.MpApp.dto.common.ForgotPasswordRequest;
import com.example.MpApp.dto.common.OtpRequest;
import com.example.MpApp.dto.common.VerifyOtpRequest;
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
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/officestaff")
@RequiredArgsConstructor
@Validated
public class OfficeStaffController {

    private final OfficeStaffService service;
    private final OfficeStaffAttendanceService attendanceService;

    // LOGIN ONLY
    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody OfficeStaffLoginRequest request) {
        return service.loginOfficeStaff(request);
    }

    // TASKS
    @GetMapping("/task/mytasks")
    public ResponseEntity<List<TaskResponse>> myTasks(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(service.getMyTasks(authHeader));
    }

    @PutMapping("/task/progress/{taskId}")
    public ResponseEntity<?> updateProgress(
            @PathVariable Long taskId,
            @RequestBody TaskUpdateRequest request,
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(service.updateProgress(taskId, authHeader,request));
    }

    @PutMapping("/task/submit/{taskId}")
    public ResponseEntity<?> submitTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(service.submitTask(taskId));
    }

    @GetMapping("/{staffId}/profile")
public ResponseEntity<OfficeStaffProfileResponse> getProfile(
        @PathVariable Long staffId,
        @RequestHeader("Authorization") String authHeader) {

    return ResponseEntity.ok(
            service.getProfile(staffId, authHeader));
}

   @PostMapping("/{staffId}/leave")
public ResponseEntity<?> requestLeave(
        @PathVariable Long staffId,
        @RequestBody LeaveRequestDTO request,
        @RequestHeader("Authorization") String authHeader) {

    return ResponseEntity.ok(
            service.requestLeave(staffId, request, authHeader));
}

@GetMapping("/{staffId}/leave-history")
public ResponseEntity<?> getMyLeaveHistory(
        @PathVariable Long staffId,
        @RequestHeader("Authorization") String authHeader) {

    return ResponseEntity.ok(
            service.getLeaveHistory(staffId, authHeader));
}
    /*
    ===================================
    REQUEST PERMISSION (1 OR 2 HOURS)
    ===================================
    */
    @PostMapping("/permissions-request")
    public ResponseEntity<Map<String, String>> requestPermission(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody PermissionRequestDTO requestDTO) {

        return ResponseEntity.ok(service.requestPermission(authHeader, requestDTO));
    }

    /*
    ===================================
    GET PERMISSION HISTORY
    ===================================
    */
    @GetMapping("/permissions/history")
    public ResponseEntity<List<PermissionResponseDTO>> getPermissionHistory(
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(service.getPermissionHistory(authHeader));
    }

    @PostMapping("/checkin")
    public ResponseEntity<OfficeStaffAttendance> checkIn(@RequestHeader("Authorization") String authHeader, @RequestBody CheckInRequestDTO checkInRequestDTO) {
        return ResponseEntity.ok(attendanceService.checkIn(authHeader,checkInRequestDTO));
    }

    @PostMapping("/checkout")
    public ResponseEntity<OfficeStaffAttendance> checkOut(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(attendanceService.checkOut(authHeader));
    }

    @GetMapping("/attendance-history")
    public ResponseEntity<List<AttendanceResponseDTO>> getHistory(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(attendanceService.getStaffAttendanceHistory(authHeader));
    }

    // ================= FORGOT & RESET PASSWORD =================
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
        return ResponseEntity.ok(service.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword()));
    }

    @GetMapping("/files/{id}")
public ResponseEntity<FileViewResponse> getStaffFiles(
        @PathVariable Long id,
        @RequestHeader("Authorization") String authHeader) {

    return ResponseEntity.ok(
            service.getStaffFiles(id, authHeader));
}

    @GetMapping("/{staffId}/performance-summary")
public ResponseEntity<PerformanceSummaryDTO> getPerformanceSummary(
        @PathVariable Long staffId,
        @RequestHeader("Authorization") String authHeader) {

    return ResponseEntity.ok(
            service.getStaffPerformanceSummary(staffId, authHeader));
}

    @GetMapping("/leaderboard")
    public ResponseEntity<List<OfficeStaff>> getLeaderboard() {
        return ResponseEntity.ok(service.getLeaderboard());
    }

    // Add this to OfficeStaffController.java

    @PatchMapping("/change-password")
    public ResponseEntity<?> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        // Identity comes from the validated token, never from the payload.
        String message = service.changePassword(
                authentication.getName(),
                request.getOldPassword(),
                request.getNewPassword()
        );

        return ResponseEntity.ok(Map.of("message", message));
    }
}