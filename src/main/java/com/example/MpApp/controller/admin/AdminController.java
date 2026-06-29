package com.example.MpApp.controller.admin;

import com.example.MpApp.dto.task.TaskAdminUpdateRequest;
import com.example.MpApp.dto.task.TaskRequest;
import com.example.MpApp.dto.task.TaskReviewRequest;
import com.example.MpApp.dto.task.TaskUpdateRequest;
import com.example.MpApp.entity.admin.Admin;
import com.example.MpApp.entity.collegestaff.CollegeStaff;
import com.example.MpApp.entity.developer_trainer_staff.TrainingBatch;
import com.example.MpApp.entity.officestaff.OfficeStaff;
import com.example.MpApp.entity.teamlead.TeamLead;
import com.example.MpApp.entity.teamlead.TeamLeadPermission;
import com.example.MpApp.service.admin.AdminService;
import com.example.MpApp.dto.admin.AdminLoginRequest;
import com.example.MpApp.service.officestaff.OfficeStaffAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService service;
    private final OfficeStaffAttendanceService attendanceService;

    // ================= AUTH =================

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Admin admin) {
        return ResponseEntity.ok(service.registerAdmin(admin));
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AdminLoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }

    @PutMapping("/update-files/{id}")
    public ResponseEntity<?> updateAdminFiles(
            @PathVariable Long id,
            @RequestParam(value = "profile", required = false) MultipartFile profile) {
        return ResponseEntity.ok(service.updateAdminFiles(id, profile));
    }

    // ================= TEAM LEAD =================

    @PostMapping("/{adminId}/teamlead")
    public ResponseEntity<?> createTeamLead(@PathVariable Long adminId,
                                            @RequestBody TeamLead teamLead) {
        return ResponseEntity.ok(service.createTeamLead(adminId, teamLead));
    }

    @PutMapping("/teamlead/update-files/{id}")
    public ResponseEntity<?> updateTeamLeadFiles(
            @PathVariable Long id,
            @RequestParam(value = "profile", required = false) MultipartFile profile,
            @RequestParam(value = "aadhaar", required = false) MultipartFile aadhaar, // [Aadhaar Redacted]
            @RequestParam(value = "resume", required = false) MultipartFile resume) {
        return ResponseEntity.ok(service.updateTeamLeadFiles(id, profile, aadhaar, resume));
    }

    @GetMapping("/teamlead")
    public ResponseEntity<?> getAllTeamLeads() {
        return ResponseEntity.ok(service.getAllTeamLeads());
    }

    @GetMapping("/teamlead/{id}")
    public ResponseEntity<?> getTeamLead(@PathVariable Long id) {
        return ResponseEntity.ok(service.getTeamLeadById(id));
    }

    @PutMapping("/teamlead/{id}")
    public ResponseEntity<?> updateTeamLead(@PathVariable Long id,
                                            @RequestBody TeamLead request) {
        return ResponseEntity.ok(service.updateTeamLead(id, request));
    }

    @DeleteMapping("/teamlead/{id}")
    public ResponseEntity<?> deleteTeamLead(@PathVariable Long id) {
        service.deleteTeamLead(id);
        return ResponseEntity.ok("Deleted");
    }

    // ================= STAFF =================

    @PostMapping("/{adminId}/staff")
    public ResponseEntity<?> createStaff(@PathVariable Long adminId,
                                         @RequestBody OfficeStaff staff) {
        return ResponseEntity.ok(service.createStaff(adminId, staff));
    }

    @PutMapping("/staff/update-files/{id}")
    public ResponseEntity<?> updateStaffFiles(
            @PathVariable Long id,
            @RequestParam(value = "profile", required = false) MultipartFile profile,
            @RequestParam(value = "aadhaar", required = false) MultipartFile aadhaar, // [Aadhaar Redacted]
            @RequestParam(value = "resume", required = false) MultipartFile resume) {
        return ResponseEntity.ok(service.updateStaffFiles(id, profile, aadhaar, resume));
    }

    @PutMapping("/staff/{id}")
    public ResponseEntity<?> updateStaff(
            @PathVariable Long id,
            @RequestBody OfficeStaff staff) {

        return ResponseEntity.ok(
                service.updateStaff(id, staff));
    }

    @GetMapping("/staff")
    public ResponseEntity<?> getAllStaff() {
        return ResponseEntity.ok(service.getAllStaff());
    }

    @GetMapping("/staff/{id}")
    public ResponseEntity<?> getStaff(@PathVariable Long id) {
        return ResponseEntity.ok(service.getStaffById(id));
    }

    @DeleteMapping("/staff/{id}")
    public ResponseEntity<?> deleteStaff(@PathVariable Long id) {
        service.deleteStaff(id);
        return ResponseEntity.ok("Deleted");
    }

    // ================= COLLEGE STAFF (NEW) =================

    @PostMapping("/college-staff")
    public ResponseEntity<?> createCollegeStaff(
            @RequestHeader("Authorization") String auth,
            @RequestBody CollegeStaff staff) {

        service.validateAdminToken(auth);
        return ResponseEntity.ok(service.createCollegeStaff(staff));
    }

    @GetMapping("/college-staff")
    public ResponseEntity<?> getAllCollegeStaff(
            @RequestHeader("Authorization") String auth) {

        service.validateAdminToken(auth);
        return ResponseEntity.ok(service.getAllCollegeStaff());
    }

    @GetMapping("/college-staff/{id}")
    public ResponseEntity<?> getCollegeStaffById(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long id) {

        service.validateAdminToken(auth);
        return ResponseEntity.ok(service.getCollegeStaffById(id));
    }

    @PutMapping("/college-staff/{id}")
    public ResponseEntity<?> updateCollegeStaff(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long id,
            @RequestBody CollegeStaff staff) {

        service.validateAdminToken(auth);
        return ResponseEntity.ok(service.updateCollegeStaff(id, staff));
    }

    @DeleteMapping("/college-staff/{id}")
    public ResponseEntity<?> deleteCollegeStaff(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long id) {

        service.validateAdminToken(auth);
        service.deleteCollegeStaff(id);
        return ResponseEntity.ok("Deleted");
    }

    @GetMapping("/staff/pending-approvals")
    public ResponseEntity<?> getPendingStaff() {
        return ResponseEntity.ok(service.getPendingStaffRegistrations());
    }

    @PatchMapping("/staff/{staffId}/approve")
    public ResponseEntity<?> approveStaff(@PathVariable Long staffId) {
        return ResponseEntity.ok(service.approveStaffRegistration(staffId));
    }

    // ================= STUDENTS (NEW) =================

    @GetMapping("/students")
    public ResponseEntity<?> getAllStudents(
            @RequestHeader("Authorization") String auth) {

        service.validateAdminToken(auth);
        return ResponseEntity.ok(service.getAllStudents());
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<?> getStudentById(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long id) {

        service.validateAdminToken(auth);
        return ResponseEntity.ok(service.getStudentById(id));
    }

    @GetMapping("/students/email")
    public ResponseEntity<?> getStudentByEmail(
            @RequestHeader("Authorization") String auth,
            @RequestParam String email) {

        service.validateAdminToken(auth);
        return ResponseEntity.ok(service.getStudentByEmail(email));
    }

    @GetMapping("/students/code/{studentId}")
    public ResponseEntity<?> getStudentByStudentId(
            @RequestHeader("Authorization") String auth,
            @PathVariable String studentId) {

        service.validateAdminToken(auth);
        return ResponseEntity.ok(service.getStudentByStudentId(studentId));
    }

    @PutMapping("/{adminId}/teamlead-leave/{leaveId}/review")
    public ResponseEntity<?> reviewTeamLeadLeave(
            @PathVariable Long adminId,
            @PathVariable Long leaveId,
            @RequestParam String status) {

        return ResponseEntity.ok(service.reviewTeamLeadLeave(adminId, leaveId, status));
    }

    @GetMapping("/teamlead-leaves")
    public ResponseEntity<?> getAllTeamLeadLeaveRequests() {
        return ResponseEntity.ok(service.getAllTeamLeadLeaveRequests());
    }

    // FETCH ALL PENDING TEAM LEAD REQUESTS
    @GetMapping("/permissions/pending")
    public ResponseEntity<List<TeamLeadPermission>> getPendingTeamLeadPermissions() {
        return ResponseEntity.ok(service.getPendingAdminPermissions());
    }

    // APPROVE TEAM LEAD REQUEST
    @PutMapping("/permissions/{permissionId}/approve")
    public ResponseEntity<Map<String, String>> approveTeamLeadPermission(
            @PathVariable Long permissionId) {
        return ResponseEntity.ok(service.approveTeamLeadPermission(permissionId));
    }

    // REJECT TEAM LEAD REQUEST
    @PutMapping("/permissions/{permissionId}/reject")
    public ResponseEntity<Map<String, String>> rejectTeamLeadPermission(
            @PathVariable Long permissionId,
            @RequestParam(required = false) String remarks) {
        return ResponseEntity.ok(service.rejectTeamLeadPermission(permissionId, remarks));
    }

    @PostMapping("/mark-holiday")
    public ResponseEntity<String> markHoliday(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate holidayDate) {
        return ResponseEntity.ok(attendanceService.markHolidayOD(holidayDate));
    }
    //=============== TASK MANAGEMENT ===================

    @PostMapping("/{adminId}/assign-task")
    public ResponseEntity<?> assignTask(@PathVariable Long adminId,
                                        @RequestBody TaskRequest request) {
        return ResponseEntity.ok(service.assignTaskByAdmin(adminId, request));
    }

    @GetMapping("/tasks")
    public ResponseEntity<?> getAllTasks(@RequestHeader("Authorization") String auth) {
        service.validateAdminToken(auth);
        return ResponseEntity.ok(service.getAllTasks());
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<?> getTaskById(@RequestHeader("Authorization") String auth, @PathVariable Long taskId) {
        service.validateAdminToken(auth);
        return ResponseEntity.ok(service.getTaskById(taskId));
    }

    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<?> updateTask(@RequestHeader("Authorization") String auth,
                                        @PathVariable Long taskId,
                                        @RequestBody TaskAdminUpdateRequest request) {
        service.validateAdminToken(auth);
        return ResponseEntity.ok(service.updateTaskAdmin(taskId, request));
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<?> deleteTask(@RequestHeader("Authorization") String auth, @PathVariable Long taskId) {
        service.validateAdminToken(auth);
        service.deleteTaskByAdmin(taskId);
        return ResponseEntity.ok("Task deleted successfully");
    }

    @PostMapping("/tasks/{taskId}/review")
    public ResponseEntity<?> reviewTask(@RequestHeader("Authorization") String auth,
                                        @PathVariable Long taskId,
                                        @RequestParam Long adminId,
                                        @RequestBody TaskReviewRequest request) {
        service.validateAdminToken(auth);
        return ResponseEntity.ok(service.reviewTaskByAdmin(taskId, adminId, request));
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
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword) {
        return ResponseEntity.ok(service.resetPassword(email, otp, newPassword));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(service.getAdminDashboardStats());
    }

    // ================= BATCH STAFF ASSIGNMENT =================
    @PostMapping("/{adminId}/training-batches")
    public ResponseEntity<?> createTrainingBatch(
            @PathVariable Long adminId,
            @RequestBody TrainingBatch batch) {
        try {
            return ResponseEntity.ok(service.createTrainingBatch(adminId, batch,batch.getCourse().getId(),batch.getOfferedCourse().getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/batches/{batchId}/assign-staff")
    public ResponseEntity<?> assignStaffToBatch(
            @PathVariable Long batchId,
            @RequestParam Long staffId) {
        try {
            return ResponseEntity.ok(service.assignStaffToBatch(batchId, staffId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/batches/{batchId}/assign-staff-bulk")
    public ResponseEntity<?> assignStaffToBatchBulk(
            @PathVariable Long batchId,
            @RequestBody List<Long> staffIds) {
        try {
            return ResponseEntity.ok(service.assignStaffToBatchBulk(batchId, staffIds));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ================= BATCH STUDENT ASSIGNMENT =================

    @PostMapping("/batches/{batchId}/assign-students")
    public ResponseEntity<?> assignStudentsToBatch(
            @PathVariable Long batchId,
            @RequestBody List<Long> studentIds) {
        try {
            return ResponseEntity.ok(service.assignStudentsToBatch(batchId, studentIds));
        } catch (Exception e) {
            // Catches any missing resources or validation errors and returns a clean 400 response
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}