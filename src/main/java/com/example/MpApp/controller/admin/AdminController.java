package com.example.MpApp.controller.admin;

import com.example.MpApp.dto.task.TaskAdminUpdateRequest;
import com.example.MpApp.dto.task.TaskRequest;
import com.example.MpApp.dto.task.TaskReviewRequest;
import com.example.MpApp.dto.task.TaskUpdateRequest;
import com.example.MpApp.dto.teamlead.TeamLeadPerformanceDTO;
import com.example.MpApp.entity.admin.Admin;
import com.example.MpApp.entity.collegestaff.CollegeStaff;
import com.example.MpApp.entity.officestaff.OfficeStaff;
import com.example.MpApp.entity.teamlead.TeamLead;
import com.example.MpApp.entity.teamlead.TeamLeadPermission;
import com.example.MpApp.service.admin.AdminService;
import com.example.MpApp.service.officestaff.OfficeStaffAttendanceService;
import com.example.MpApp.service.teamlead.TeamLeadService;

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
    private final TeamLeadService teamLeadService;


    // ==========================================================
    // AUTH
    // ==========================================================

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Admin admin) {

        return ResponseEntity.ok(
                service.registerAdmin(admin)
        );
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody com.example.MpApp.dto.admin.AdminLoginRequest request) {

        return ResponseEntity.ok(
                service.login(request)
        );
    }


    @PutMapping("/update-files/{id}")
    public ResponseEntity<?> updateAdminFiles(
            @PathVariable Long id,
            @RequestParam(value = "profile", required = false)
            MultipartFile profile) {

        return ResponseEntity.ok(
                service.updateAdminFiles(id, profile)
        );
    }


    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {

        return ResponseEntity.ok(
                service.getAdminDashboardStats()
        );
    }


    // ==========================================================
    // STAFF PENDING APPROVALS
    // ==========================================================

    @GetMapping("/staff/pending-approvals")
    public ResponseEntity<?> getPendingStaff() {

        return ResponseEntity.ok(
                service.getPendingStaffRegistrations()
        );
    }


    @PatchMapping("/staff/{staffId}/approve")
    public ResponseEntity<?> approveStaff(
            @PathVariable Long staffId) {

        return ResponseEntity.ok(
                service.approveStaffRegistration(staffId)
        );
    }


    // ==========================================================
    // TEAM LEAD
    // ==========================================================

    @PostMapping("/teamlead")
    public ResponseEntity<?> createTeamLead(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody TeamLead teamLead) {

        return ResponseEntity.ok(
                service.createTeamLeadFromToken(authHeader, teamLead)
        );
    }


    @PutMapping("/teamlead/update-files/{id}")
    public ResponseEntity<?> updateTeamLeadFiles(
            @PathVariable Long id,
            @RequestParam(value = "profile", required = false)
            MultipartFile profile,
            @RequestParam(value = "aadhaar", required = false)
            MultipartFile aadhaar,
            @RequestParam(value = "resume", required = false)
            MultipartFile resume) {

        return ResponseEntity.ok(
                service.updateTeamLeadFiles(
                        id,
                        profile,
                        aadhaar,
                        resume
                )
        );
    }


    @GetMapping("/teamlead")
    public ResponseEntity<?> getAllTeamLeads() {

        return ResponseEntity.ok(
                service.getAllTeamLeads()
        );
    }


    @GetMapping("/teamlead/{id}")
    public ResponseEntity<?> getTeamLead(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.getTeamLeadById(id)
        );
    }


    @PutMapping("/teamlead/{id}")
    public ResponseEntity<?> updateTeamLead(
            @PathVariable Long id,
            @RequestBody TeamLead request) {

        return ResponseEntity.ok(
                service.updateTeamLead(id, request)
        );
    }


    @DeleteMapping("/teamlead/{id}")
    public ResponseEntity<?> deleteTeamLead(
            @PathVariable Long id) {

        service.deleteTeamLead(id);

        return ResponseEntity.ok("Deleted");
    }


    @PatchMapping("/teamlead/{id}/status")
    public ResponseEntity<?> toggleTeamLeadStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {

        return ResponseEntity.ok(
                service.toggleTeamLeadStatus(id, active)
        );
    }


    @GetMapping("/teamlead/{id}/performance")
    public ResponseEntity<TeamLeadPerformanceDTO>
    getTeamLeadPerformance(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.getTeamLeadPerformance(id)
        );
    }


    // ==========================================================
    // OFFICE STAFF
    // ==========================================================

    @PostMapping("/{adminId}/staff")
    public ResponseEntity<?> createStaff(
            @PathVariable Long adminId,
            @RequestBody OfficeStaff staff) {

        return ResponseEntity.ok(
                service.createStaff(adminId, staff)
        );
    }


    @PutMapping("/staff/update-files/{id}")
    public ResponseEntity<?> updateStaffFiles(
            @PathVariable Long id,
            @RequestParam(value = "profile", required = false)
            MultipartFile profile,
            @RequestParam(value = "aadhaar", required = false)
            MultipartFile aadhaar,
            @RequestParam(value = "resume", required = false)
            MultipartFile resume) {

        return ResponseEntity.ok(
                service.updateStaffFiles(
                        id,
                        profile,
                        aadhaar,
                        resume
                )
        );
    }


    @PutMapping("/staff/{id}")
    public ResponseEntity<?> updateStaff(
            @PathVariable Long id,
            @RequestBody OfficeStaff staff) {

        return ResponseEntity.ok(
                service.updateStaff(id, staff)
        );
    }

   


    @GetMapping("/staff")
    public ResponseEntity<?> getAllStaff() {

        return ResponseEntity.ok(
                service.getAllStaff()
        );
    }


    @GetMapping("/staff/{id}")
    public ResponseEntity<?> getStaff(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.getStaffById(id)
        );
    }


    @DeleteMapping("/staff/{id}")
    public ResponseEntity<?> deleteStaff(
            @PathVariable Long id) {

        service.deleteStaff(id);

        return ResponseEntity.ok("Deleted");
    }


    // ==========================================================
    // COLLEGE STAFF
    // ==========================================================

    @PostMapping("/college-staff")
    public ResponseEntity<?> createCollegeStaff(
            @RequestHeader("Authorization") String auth,
            @RequestBody CollegeStaff staff) {

        service.validateAdminToken(auth);

        return ResponseEntity.ok(
                service.createCollegeStaff(staff)
        );
    }


    @GetMapping("/college-staff")
    public ResponseEntity<?> getAllCollegeStaff(
            @RequestHeader("Authorization") String auth) {

        service.validateAdminToken(auth);

        return ResponseEntity.ok(
                service.getAllCollegeStaff()
        );
    }


    @GetMapping("/college-staff/{id}")
    public ResponseEntity<?> getCollegeStaffById(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long id) {

        service.validateAdminToken(auth);

        return ResponseEntity.ok(
                service.getCollegeStaffById(id)
        );
    }


    @PutMapping("/college-staff/{id}")
    public ResponseEntity<?> updateCollegeStaff(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long id,
            @RequestBody CollegeStaff staff) {

        service.validateAdminToken(auth);

        return ResponseEntity.ok(
                service.updateCollegeStaff(id, staff)
        );
    }


    @DeleteMapping("/college-staff/{id}")
    public ResponseEntity<?> deleteCollegeStaff(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long id) {

        service.validateAdminToken(auth);

        service.deleteCollegeStaff(id);

        return ResponseEntity.ok("Deleted");
    }


    // ==========================================================
    // STUDENTS
    // ==========================================================

    @GetMapping("/students")
    public ResponseEntity<?> getAllStudents(
            @RequestHeader("Authorization") String auth) {

        service.validateAdminToken(auth);

        return ResponseEntity.ok(
                service.getAllStudents()
        );
    }


    @GetMapping("/students/{id}")
    public ResponseEntity<?> getStudentById(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long id) {

        service.validateAdminToken(auth);

        return ResponseEntity.ok(
                service.getStudentById(id)
        );
    }


    @GetMapping("/students/email")
    public ResponseEntity<?> getStudentByEmail(
            @RequestHeader("Authorization") String auth,
            @RequestParam String email) {

        service.validateAdminToken(auth);

        return ResponseEntity.ok(
                service.getStudentByEmail(email)
        );
    }


    @GetMapping("/students/code/{studentId}")
    public ResponseEntity<?> getStudentByStudentId(
            @RequestHeader("Authorization") String auth,
            @PathVariable String studentId) {

        service.validateAdminToken(auth);

        return ResponseEntity.ok(
                service.getStudentByStudentId(studentId)
        );
    }


    // ==========================================================
    // LEAVE / PERMISSION / HOLIDAY
    // ==========================================================

    @PutMapping("/{adminId}/teamlead-leave/{leaveId}/review")
    public ResponseEntity<?> reviewTeamLeadLeave(
            @PathVariable Long adminId,
            @PathVariable Long leaveId,
            @RequestParam String status) {

        return ResponseEntity.ok(
                service.reviewTeamLeadLeave(
                        adminId,
                        leaveId,
                        status
                )
        );
    }


    @GetMapping("/teamlead-leaves")
    public ResponseEntity<?> getAllTeamLeadLeaveRequests() {

        return ResponseEntity.ok(
                service.getAllTeamLeadLeaveRequests()
        );
    }


    @GetMapping("/teamlead/permissions/pending")
    public ResponseEntity<?>
    getPendingTeamLeadPermissions() {

        return ResponseEntity.ok(
                service.getPendingAdminPermissions()
        );
    }


    @PutMapping("/teamlead/permissions/approve/{permissionId}")
    public ResponseEntity<Map<String, String>>
    approveTeamLeadPermission(
            @PathVariable Long permissionId) {

        return ResponseEntity.ok(
                service.approveTeamLeadPermission(permissionId)
        );
    }


    @PutMapping("/teamlead/permissions/reject/{permissionId}")
    public ResponseEntity<Map<String, String>>
    rejectTeamLeadPermission(
            @PathVariable Long permissionId,
            @RequestParam(required = false) String remarks) {

        return ResponseEntity.ok(
                service.rejectTeamLeadPermission(
                        permissionId,
                        remarks
                )
        );
    }


    @PatchMapping("/mark-holiday/officeStaff/all")
    public ResponseEntity<Map<String,String>> markHoliday(
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate holidayDate) {

        return ResponseEntity.ok(
                attendanceService.markHolidayOD(holidayDate)
        );
    }

    @PatchMapping("/mark-holiday/teamlead/all")
    public ResponseEntity<Map<String,String>> markHolidayTL(
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate holidayDate
    ){
        return ResponseEntity.ok(
                teamLeadService.markHolidayODForTeamLeads(holidayDate)
        );
    }

    @PatchMapping("/mark-holiday/teamlead/{teamleadId}")
    public ResponseEntity<Map<String,String>> markHolidayTL(
            @PathVariable Long teamleadId,
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate holidayDate
    ){
        return ResponseEntity.ok(
                teamLeadService.markHolidayODForSingleTeamLead(teamleadId, holidayDate)
        );

    }

    @PatchMapping("/mark-holiday/officeStaff/{staffId}")
    public ResponseEntity<Map<String,String>> markHolidayODForStaff(
            @PathVariable Long staffId,
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
           LocalDate holidayDate) {
        return ResponseEntity.ok(
                attendanceService.markHolidayODForSingleStaff(staffId, holidayDate)
        );
    }


    // ==========================================================
    // TASK MANAGEMENT
    // ==========================================================

    @PostMapping("/{adminId}/assign-task")
    public ResponseEntity<?> assignTask(
            @PathVariable Long adminId,
            @RequestBody TaskRequest request) {

        return ResponseEntity.ok(
                service.assignTaskByAdmin(
                        adminId,
                        request
                )
        );
    }


    @GetMapping("/tasks")
    public ResponseEntity<?> getAllTasks(
            @RequestHeader("Authorization") String auth) {

        service.validateAdminToken(auth);

        return ResponseEntity.ok(
                service.getAllTasks()
        );
    }


    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<?> getTaskById(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long taskId) {

        service.validateAdminToken(auth);

        return ResponseEntity.ok(
                service.getTaskById(taskId)
        );
    }


    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<?> updateTask(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long taskId,
            @RequestBody TaskAdminUpdateRequest request) {

        service.validateAdminToken(auth);

        return ResponseEntity.ok(
                service.updateTaskAdmin(
                        taskId,
                        request
                )
        );
    }


    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<?> deleteTask(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long taskId) {

        service.validateAdminToken(auth);

        service.deleteTaskByAdmin(taskId);

        return ResponseEntity.ok(
                "Task deleted successfully"
        );
    }


    @PostMapping("/tasks/{taskId}/review")
    public ResponseEntity<?> reviewTask(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long taskId,
            @RequestParam Long adminId,
            @RequestBody TaskReviewRequest request) {

        service.validateAdminToken(auth);

        return ResponseEntity.ok(
                service.reviewTaskByAdmin(
                        taskId,
                        adminId,
                        request
                )
        );
    }


    // ==========================================================
    // FORGOT & RESET PASSWORD
    // ==========================================================

    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<?> sendOtp(
            @RequestParam String email) {

        return ResponseEntity.ok(
                service.sendOtp(email)
        );
    }


    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @RequestParam String email,
            @RequestParam String otp) {

        return ResponseEntity.ok(
                service.verifyOtp(email, otp)
        );
    }


    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPassword(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword) {

        return ResponseEntity.ok(
                service.resetPassword(
                        email,
                        otp,
                        newPassword
                )
        );
    }


    // ==========================================================
    // GET LEAVES MAP PER DATE FOR CALENDAR UI
    // ==========================================================

    @GetMapping("/calendar/leaves")
    public ResponseEntity<
            Map<LocalDate, List<Map<String, Object>>>>
    getCalendarLeaveRegistry(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        return ResponseEntity.ok(
                attendanceService.getLeavesByDateRange(
                        startDate,
                        endDate
                )
        );
    }


    // ==========================================================
    // TOGGLE SATURDAY HOLIDAY RULE
    // ==========================================================

    @PostMapping("/schedule/saturday-working")
    public ResponseEntity<Map<String, String>>
    toggleSaturdayWorkingShift(
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate targetSaturday) {

        String responseMessage =
                attendanceService.configureSaturdayAsWorkingDay(
                        targetSaturday
                );

        return ResponseEntity.ok(
                Map.of(
                        "status", "SUCCESS",
                        "message", responseMessage,
                        "effectiveDate",
                        targetSaturday.toString()
                )
        );
    }
}