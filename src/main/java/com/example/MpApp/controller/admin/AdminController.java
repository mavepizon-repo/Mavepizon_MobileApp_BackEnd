package com.example.MpApp.controller.admin;

import com.example.MpApp.entity.admin.Admin;
import com.example.MpApp.entity.collegestaff.CollegeStaff;
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

    // ================= TEAM LEAD =================

    @PostMapping("/{adminId}/teamlead")
    public ResponseEntity<?> createTeamLead(@PathVariable Long adminId,
                                            @RequestBody TeamLead teamLead) {
        return ResponseEntity.ok(service.createTeamLead(adminId, teamLead));
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
}