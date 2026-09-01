package com.example.MpApp.controller.teamlead;

import com.example.MpApp.dto.common.ForgotPasswordRequest;
import com.example.MpApp.dto.officestaff.CheckInRequestDTO;
import com.example.MpApp.dto.officestaff.LeaveRequestDTO;
import com.example.MpApp.dto.officestaff.OfficeStaffPermissionResponseDTO;
import com.example.MpApp.dto.officestaff.OfficeStaffResponseDTO;
import com.example.MpApp.dto.task.*;
import com.example.MpApp.dto.teamlead.TeamLeadLoginRequest;
import com.example.MpApp.dto.teamlead.TeamLeadPermissionRequestDTO;
import com.example.MpApp.dto.teamlead.TeamLeadProfileDTO;

import com.example.MpApp.entity.collegestaff.CollegeStaff;
import com.example.MpApp.entity.course.Course;
import com.example.MpApp.entity.enums.TaskStatus;
import com.example.MpApp.entity.officestaff.OfficeStaff;
import com.example.MpApp.entity.officestaff.OfficeStaffPermission;
import com.example.MpApp.entity.student.Student;
import com.example.MpApp.entity.task.Task;
import com.example.MpApp.entity.task.TaskReview;
import com.example.MpApp.entity.teamlead.TeamLeadAttendance;
import com.example.MpApp.entity.teamlead.TeamLeadLeave;
import com.example.MpApp.entity.teamlead.TeamLeadPermission;

import com.example.MpApp.service.course.CourseService;
import com.example.MpApp.service.teamlead.TeamLeadAttendanceService;
import com.example.MpApp.service.teamlead.TeamLeadService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TeamLeadController {


    private final TeamLeadService service;

    private final TeamLeadAttendanceService teamLeadAttendanceService;

    private final CourseService courseService;


    // ==========================================================
    // TEAM LEAD LOGIN
    // ==========================================================

    @PostMapping("/teamlead/login")
    public ResponseEntity<?> login(
            @RequestBody TeamLeadLoginRequest request) {

        return ResponseEntity.ok(
                service.loginTeamLead(request)
        );
    }


    // ==========================================================
    // COURSE MANAGEMENT
    // ==========================================================
    /*
     * IMPORTANT:
     *
     * Course creation is handled ONLY by CourseService.
     *
     * TeamLeadController does NOT contain
     * duplicate course creation logic.
     *
     * CourseService handles:
     *
     * 1. Course validation
     * 2. Course code generation
     * 3. Batch ID generation
     * 4. Fee validation
     * 5. Seat validation
     * 6. Registration fee calculation
     * 7. Course save
     */

    /*
     * CREATE COURSE
     *
     * POST
     * /api/teamlead/{teamLeadId}/courses
     *
     * teamLeadId is kept in the URL so that
     * the Team Lead frontend can use the same
     * Team Lead API structure.
     *
     * Course creation itself is delegated
     * completely to CourseService.
     */

    @PostMapping("/{teamLeadId}/courses")
    public ResponseEntity<?> createCourse(
            @PathVariable Long teamLeadId,
            @RequestBody Course course) {

        try {

            return ResponseEntity.ok(
                    courseService.createCourse(course)
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }


    // ==========================================================
    // STAFF MANAGEMENT
    // ==========================================================

    @PostMapping("/office-staff/create")
    public ResponseEntity<?> createStaff(
            @RequestHeader("Authorization") String authHeader,@RequestBody OfficeStaff staff) {

        Map<String, String> response =
                service.createStaff(
                        authHeader,
                        staff
                );

        if (response == null) {

            Map<String, String> map =
                    new HashMap<>();

            map.put(
                    "message",
                    "Staff is already exist"
            );

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(map);
        }

        return ResponseEntity.ok(response);
    }


    @PutMapping("/office-staff/update-files/{id}")
    public ResponseEntity<?> updateStaffFiles(
            @PathVariable Long id,
            @RequestParam(
                    value = "profile",
                    required = false
            )
            MultipartFile profile,

            @RequestParam(
                    value = "aadhaar",
                    required = false
            )
            MultipartFile aadhaar,

            @RequestParam(
                    value = "resume",
                    required = false
            )
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


    @PutMapping("/staff/update/{staffId}")
    public ResponseEntity<?> updateStaff(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long staffId,
            @RequestBody OfficeStaff staff) {

        return ResponseEntity.ok(
                service.updateStaff(
                        authHeader,
                        staffId,
                        staff
                )
        );
    }


    @GetMapping("/staff/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<OfficeStaffResponseDTO>> getAllStaff(
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(
                service.getAllStaff()
        );
    }

    @GetMapping("/staff/all-by-teamlead")
    public ResponseEntity<List<OfficeStaffResponseDTO>> getAllStaffByTeamLeadId(
            @RequestHeader("Authorization") String authHeader
    ) {
        return ResponseEntity.ok(service.getAllStaffByTeamLead(authHeader));
    }


    @GetMapping("/staff/{staffId}")
    public ResponseEntity<OfficeStaffResponseDTO> getStaffById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long staffId) {

        return ResponseEntity.ok(
                service.getStaffById(
                        authHeader,
                        staffId
                )
        );
    }


    @DeleteMapping("/staff/{staffId}")
    public ResponseEntity<?> deleteStaff(
            @PathVariable Long staffId) {

        service.deleteStaff(staffId);

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "message",
                "deleted successfully"
        );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{teamLeadId}/profile")
    public ResponseEntity<TeamLeadProfileDTO> getProfile(
            @PathVariable Long teamLeadId) {

        return ResponseEntity.ok(
                service.getTeamLeadProfile(teamLeadId)
        );
    }


    // ==========================================================
    // TASK MANAGEMENT
    // ==========================================================

    @PostMapping("/task/assign")
    @PreAuthorize("hasAnyRole('ROLE_TEAM_LEAD','ROLE_ADMIN')")
    public ResponseEntity<?> assignTask(@RequestBody TaskRequest request,  @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(
                service.assignTask(
                        authHeader,
                        request
                )
        );
    }

    @PostMapping("/task/assign-some")
    @PreAuthorize("hasRole('ROLE_TEAM_LEAD')")
    public ResponseEntity<?> assignSome(
            @RequestBody TaskRequestSome request ,
            @RequestHeader("Authorization") String authHeader
            ){

        return ResponseEntity.ok(
                service.assignTaskToSome(authHeader,request)
        );

    }

    @PostMapping("/task/assign-all-by-current-user")
    @PreAuthorize("hasAnyRole('ROLE_TEAM_LEAD','ROLE_ADMIN')")
    public ResponseEntity<?> assignAllByTeamLead(@RequestHeader("Authorization") String authHeader , @RequestBody TaskRequest request){

        return ResponseEntity.ok(
                service.assignWorkToAllStaff(authHeader,request)
        );

    }


    @PutMapping("/task/update/{taskId}")
    @PreAuthorize("hasAnyRole('ROLE_TEAM_LEAD','ROLE_ADMIN')")
    public ResponseEntity<?> updateTask(
            @PathVariable Long taskId,
            @RequestBody TaskAdminUpdateRequest request,
            @RequestHeader("Authorization") String authHeader
            ) {

        return ResponseEntity.ok(
                service.updateTaskAdmin(
                        taskId,
                        authHeader,
                        request
                )
        );
    }


    @DeleteMapping("/task/{taskId}")
    @PreAuthorize("hasAnyRole('ROLE_TEAM_LEAD','ROLE_ADMIN')")
    public ResponseEntity<String> deleteTask(
            @PathVariable Long taskId) {

        service.deleteTask(taskId);

        return ResponseEntity.ok(
                "Task deleted"
        );
    }


    @GetMapping("/task/get-all")
    @PreAuthorize("hasRole('ROLE_TEAM_LEAD')")
    public ResponseEntity<List<TaskResponse>>
    getAllTasksByTeamLead(@RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(
                service.getAllTasksByTeamLead(authHeader)
        );
    }


    @GetMapping("/tasks/{taskId}")
    @PreAuthorize("hasAnyRole('ROLE_TEAM_LEAD','ROLE_ADMIN')")
    public ResponseEntity<TaskResponse> getTask(
            @PathVariable Long taskId) {

        return ResponseEntity.ok(
                service.getTaskById(taskId)
        );
    }


    @GetMapping("/tasks/staff/{staffId}")
    @PreAuthorize("hasAnyRole('ROLE_TEAM_LEAD','ROLE_ADMIN')")
    public ResponseEntity<List<TaskResponse>>
    getByStaff(
            @PathVariable Long staffId) {

        return ResponseEntity.ok(
                service.getTasksByStaff(staffId)
        );
    }


    @GetMapping("/tasks/status/{status}")
    @PreAuthorize("hasAnyRole('ROLE_TEAM_LEAD','ROLE_ADMIN')")
    public ResponseEntity<List<TaskResponse>>
    getByStatus(
            @PathVariable TaskStatus status) {

        return ResponseEntity.ok(
                service.getTasksByStatus(status)
        );
    }


    @GetMapping("/tasks/date-range")
    @PreAuthorize("hasAnyRole('ROLE_TEAM_LEAD','ROLE_ADMIN')")
    public ResponseEntity<List<TaskResponse>>
    getByDateRange(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {

        return ResponseEntity.ok(
                service.getTasksBetweenDates(
                        start,
                        end
                )
        );
    }


    // ==========================================================
    // TASK REVIEW
    // ==========================================================

    @PostMapping("/task/review/{taskId}")
    @PreAuthorize("hasAnyRole('ROLE_TEAM_LEAD','ROLE_ADMIN')")
    public ResponseEntity<?> reviewTask(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long taskId,
            @RequestBody TaskReviewRequest request) {

        return ResponseEntity.ok(
                service.reviewTask(
                        taskId,
                        authHeader,
                        request
                )
        );
    }


    @GetMapping("/reviews/pending")
    public ResponseEntity<List<Task>>
    pendingReviews() {

        return ResponseEntity.ok(
                service.getPendingReviewTasks()
        );
    }


    @GetMapping("/reviews/task/{taskId}")
    public ResponseEntity<List<TaskReview>>
    taskReviews(
            @PathVariable Long taskId) {

        return ResponseEntity.ok(
                service.getTaskReviews(taskId)
        );
    }


    // ==========================================================
    // COLLEGE STAFF MANAGEMENT
    // ==========================================================

    @PostMapping("/{teamLeadId}/college-staff")
    public ResponseEntity<?> createCollegeStaff(
            @PathVariable Long teamLeadId,
            @RequestBody CollegeStaff collegeStaff) {

        return ResponseEntity.ok(
                service.createCollegeStaff(
                        teamLeadId,
                        collegeStaff
                )
        );
    }


    @GetMapping("/{teamLeadId}/college-staff")
    public ResponseEntity<List<CollegeStaff>>
    getAllCollegeStaff(
            @PathVariable Long teamLeadId) {

        return ResponseEntity.ok(
                service.getAllCollegeStaff(
                        teamLeadId
                )
        );
    }


    @GetMapping(
            "/{teamLeadId}/college-staff/{collegeStaffId}"
    )
    public ResponseEntity<CollegeStaff>
    getCollegeStaff(
            @PathVariable Long teamLeadId,
            @PathVariable Long collegeStaffId) {

        return ResponseEntity.ok(
                service.getCollegeStaffById(
                        teamLeadId,
                        collegeStaffId
                )
        );
    }


    @PutMapping(
            "/{teamLeadId}/college-staff/{collegeStaffId}"
    )
    public ResponseEntity<?> updateCollegeStaff(
            @PathVariable Long teamLeadId,
            @PathVariable Long collegeStaffId,
            @RequestBody CollegeStaff collegeStaff) {

        return ResponseEntity.ok(
                service.updateCollegeStaff(
                        teamLeadId,
                        collegeStaffId,
                        collegeStaff
                )
        );
    }


    @DeleteMapping(
            "/{teamLeadId}/college-staff/{collegeStaffId}"
    )
    public ResponseEntity<String> deleteCollegeStaff(
            @PathVariable Long teamLeadId,
            @PathVariable Long collegeStaffId) {

        service.deleteCollegeStaff(
                teamLeadId,
                collegeStaffId
        );

        return ResponseEntity.ok(
                "College Staff Deleted Successfully"
        );
    }


    // ==========================================================
    // STUDENTS
    // ==========================================================

    @GetMapping("/{teamLeadId}/students")
    public ResponseEntity<List<Student>>
    getAllStudents(
            @PathVariable Long teamLeadId) {

        return ResponseEntity.ok(
                service.getAllStudents(teamLeadId)
        );
    }


    @GetMapping("/{teamLeadId}/students/{studentId}")
    public ResponseEntity<Student>
    getStudentById(
            @PathVariable Long teamLeadId,
            @PathVariable Long studentId) {

        return ResponseEntity.ok(
                service.getStudentById(
                        teamLeadId,
                        studentId
                )
        );
    }


    @GetMapping("/{teamLeadId}/students/code/{studentId}")
    public ResponseEntity<Student>
    getByStudentCode(
            @PathVariable Long teamLeadId,
            @PathVariable String studentId) {

        return ResponseEntity.ok(
                service.getStudentByStudentId(
                        teamLeadId,
                        studentId
                )
        );
    }


    @GetMapping("/{teamLeadId}/students/email")
    public ResponseEntity<Student>
    getByEmail(
            @PathVariable Long teamLeadId,
            @RequestParam String email) {

        return ResponseEntity.ok(
                service.getStudentByEmail(
                        teamLeadId,
                        email
                )
        );
    }


    // ==========================================================
    // STAFF LEAVE
    // ==========================================================

    @PutMapping(
            "/{teamLeadId}/leave/{leaveId}/review"
    )
    public ResponseEntity<?> reviewStaffLeave(
            @PathVariable Long teamLeadId,
            @PathVariable Long leaveId,
            @RequestParam String status) {

        return ResponseEntity.ok(
                service.reviewStaffLeave(
                        teamLeadId,
                        leaveId,
                        status
                )
        );
    }


    // ==========================================================
    // TEAM LEAD LEAVE REQUEST
    // ==========================================================

    @PostMapping("/{teamLeadId}/leave-request")
    public ResponseEntity<?> requestLeaveFromAdmin(
            @PathVariable Long teamLeadId,
            @RequestBody LeaveRequestDTO request) {

        return ResponseEntity.ok(
                service.requestLeaveFromAdmin(
                        teamLeadId,
                        request
                )
        );
    }


    @GetMapping("/office-staff/leave")
    public ResponseEntity<?> getAllOfficeStaffLeaveRequests() {

        return ResponseEntity.ok(
                service.getAllOfficeStaffLeaves()
        );
    }


    // ==========================================================
    // PERMISSION APPROVAL
    // ==========================================================

    @PutMapping(
            "/teamlead/officestaff/permissions/approve/{permissionId}"
    )
    public ResponseEntity<Map<String, String>>
    approvePermission(
            @PathVariable Long permissionId) {

        Map<String, String> response =
                service.approvePermission(
                        permissionId
                );

        return ResponseEntity.ok(response);
    }


    @PutMapping(
            "/teamlead/officestaff/permissions/reject/{permissionId}"
    )
    public ResponseEntity<Map<String, String>>
    rejectPermission(
            @PathVariable Long permissionId,
            @RequestParam(required = false)
            String remarks) {

        Map<String, String> response =
                service.rejectPermission(
                        permissionId,
                        remarks
                );

        return ResponseEntity.ok(response);
    }


    // ==========================================================
    // TEAM LEAD LEAVE / PERMISSION HISTORY
    // ==========================================================

    @GetMapping("/{teamLeadId}/leave-history")
    public ResponseEntity<List<TeamLeadLeave>>
    getMyLeaveHistory(
            @PathVariable Long teamLeadId) {

        return ResponseEntity.ok(
                service.getMyLeaveHistory(
                        teamLeadId
                )
        );
    }


    @GetMapping(
            "/teamlead/permissions/history"
    )
    public ResponseEntity<List<TeamLeadPermission>>
    getMyPermissionHistory(
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(
                service.getMyPermissionHistory(
                        authHeader
                )
        );
    }


    // ==========================================================
    // BRANCH PERMISSIONS
    // ==========================================================

    @GetMapping("/officeStaff/permissions/byBranch")
    public ResponseEntity<List<OfficeStaffPermissionResponseDTO>>
    getAllBranchPermissions(
            @RequestHeader("Authorization") String authHeader) {

        List<OfficeStaffPermissionResponseDTO> permissions =
                service.getAllBranchPermissions(
                        authHeader
                );

        return ResponseEntity.ok(permissions);
    }


    @GetMapping(
            "/teamlead/officeStaff/permissions/pending"
    )
    public ResponseEntity<List<OfficeStaffPermissionResponseDTO>>
    getPendingBranchPermissions(
            @RequestHeader("Authorization") String authHeader) {

        List<OfficeStaffPermissionResponseDTO> pendingRequests =
                service.getPendingBranchPermissions(
                        authHeader
                );

        return ResponseEntity.ok(
                pendingRequests
        );
    }


    // ==========================================================
    // TEAM LEAD PERMISSION REQUEST
    // ==========================================================

    @PostMapping(
            "/teamlead/permission-request"
    )
    public ResponseEntity<?> requestPermissionFromAdmin(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody TeamLeadPermissionRequestDTO request) {

        return ResponseEntity.ok(
                service.requestPermissionToAdmin(
                        authHeader,
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
                service.verifyOtp(
                        email,
                        otp
                )
        );
    }


    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPassword(
            @RequestBody
            ForgotPasswordRequest forgotPasswordRequest) {

        return ResponseEntity.ok(
                service.resetPassword(
                        forgotPasswordRequest.getEmail(),
                        forgotPasswordRequest.getOtp(),
                        forgotPasswordRequest.getNewPassword()
                )
        );
    }


    // ==========================================================
    // STAFF STATUS
    // ==========================================================

    @PatchMapping("/staff/{staffId}/status")
    public ResponseEntity<Map<String, String>>
    toggleStaffStatus(
            @PathVariable Long staffId,
            @RequestParam boolean active) {

        return ResponseEntity.ok(
                service.changeStaffStatus(
                        staffId,
                        active
                )
        );
    }


    // ==========================================================
    // ATTENDANCE
    // ==========================================================

    @PatchMapping("/teamlead/checkin")
    public ResponseEntity<TeamLeadAttendance>
    checkIn(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CheckInRequestDTO request) {

        return ResponseEntity.ok(
                teamLeadAttendanceService.checkIn(
                        authHeader,
                        request
                )
        );
    }


    @PatchMapping("/teamlead/checkout")
    public ResponseEntity<?> checkOut(
            @RequestHeader("Authorization") String authHeader) {

        try {

            return ResponseEntity.ok(
                    teamLeadAttendanceService.checkOut(
                            authHeader
                    )
            );

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }


    @GetMapping(
            "/{teamLeadId}/attendance-history"
    )
    public ResponseEntity<List<TeamLeadAttendance>>
    getHistory(
            @PathVariable Long teamLeadId) {

        return ResponseEntity.ok(
                teamLeadAttendanceService.getHistory(
                        teamLeadId
                )
        );
    }


    // ==========================================================
    // CHANGE PASSWORD
    // ==========================================================

    @PatchMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody Map<String, String> request) {

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        service.changePassword(
                                request.get("email"),
                                request.get("oldPassword"),
                                request.get("newPassword")
                        )
                )
        );
    }


    // ==========================================================
    // TEAM LEAD DASHBOARD
    // ==========================================================

    @GetMapping("/{teamLeadId}/dashboard")
    public ResponseEntity<Map<String, Object>>
    getDashboardStats(
            @PathVariable Long teamLeadId) {

        return ResponseEntity.ok(
                service.getTeamLeadDashboardStats(
                        teamLeadId
                )
        );
    }

}