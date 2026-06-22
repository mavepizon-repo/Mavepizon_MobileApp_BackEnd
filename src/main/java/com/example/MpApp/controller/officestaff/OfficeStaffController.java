package com.example.MpApp.controller.officestaff;

import com.example.MpApp.dto.officestaff.LeaveRequestDTO;
import com.example.MpApp.dto.officestaff.OfficeStaffProfileResponse;
import com.example.MpApp.dto.officestaff.PermissionRequestDTO;
import com.example.MpApp.entity.officestaff.OfficeStaffAttendance;
import com.example.MpApp.entity.officestaff.OfficeStaffPermission;
import com.example.MpApp.service.officestaff.OfficeStaffAttendanceService;
import com.example.MpApp.service.officestaff.OfficeStaffService;
import com.example.MpApp.dto.officestaff.OfficeStaffLoginRequest;
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
    public ResponseEntity<OfficeStaffAttendance> checkIn(@PathVariable Long staffId) {
        return ResponseEntity.ok(attendanceService.checkIn(staffId));
    }

    @PostMapping("/{staffId}/checkout")
    public ResponseEntity<OfficeStaffAttendance> checkOut(@PathVariable Long staffId) {
        return ResponseEntity.ok(attendanceService.checkOut(staffId));
    }

    @GetMapping("/{staffId}/history")
    public ResponseEntity<List<OfficeStaffAttendance>> getHistory(@PathVariable Long staffId) {
        return ResponseEntity.ok(attendanceService.getStaffAttendanceHistory(staffId));
    }
}