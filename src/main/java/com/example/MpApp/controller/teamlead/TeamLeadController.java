    package com.example.MpApp.controller.teamlead;

    import com.example.MpApp.dto.officestaff.LeaveRequestDTO;
    import com.example.MpApp.dto.task.TaskRequest;
    import com.example.MpApp.dto.task.TaskResponse;
    import com.example.MpApp.dto.task.TaskReviewRequest;
    import com.example.MpApp.dto.task.TaskUpdateRequest;
    import com.example.MpApp.dto.teamlead.TeamLeadLoginRequest;
    import com.example.MpApp.dto.teamlead.TeamLeadPermissionRequestDTO;
    import com.example.MpApp.entity.collegestaff.CollegeStaff;
    import com.example.MpApp.entity.officestaff.OfficeStaff;
    import com.example.MpApp.entity.officestaff.OfficeStaffPermission;
    import com.example.MpApp.entity.student.Student;
    import com.example.MpApp.entity.task.Task;
    import com.example.MpApp.entity.task.TaskReview;
    import com.example.MpApp.service.teamlead.TeamLeadService;
    import com.example.MpApp.entity.enums.TaskStatus;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import java.time.LocalDate;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    @RestController
    @RequestMapping("/api/teamlead")
    @RequiredArgsConstructor
    public class TeamLeadController {

        private final TeamLeadService service;

        //LOGIN FOR TEAM LEAD
        @PostMapping("/login")
        public ResponseEntity<?> login(@RequestBody TeamLeadLoginRequest request) {
            return ResponseEntity.ok(service.loginTeamLead(request));
        }

        // STAFF MANAGEMENT (ONLY TEAM LEAD)
        @PostMapping("/{teamLeadId}/staff")
        public ResponseEntity<?> createStaff(
                @PathVariable Long teamLeadId,
                @RequestBody OfficeStaff staff) {
            Map<String,String> response=service.createStaff(teamLeadId, staff);
            if(response==null){
                Map<String,String> map=new HashMap<>();
                map.put("message","Staff is already exist");
                return ResponseEntity.status(404).body(map);
            }
            return ResponseEntity.ok(response);
        }

        @PutMapping("/{teamLeadId}/staff/{staffId}")
        public ResponseEntity<?> updateStaff(
                @PathVariable Long teamLeadId,
                @PathVariable Long staffId,
                @RequestBody OfficeStaff staff) {

            return ResponseEntity.ok(service.updateStaff(teamLeadId, staffId, staff));
        }

        @GetMapping("/{teamLeadId}/staff")
        public ResponseEntity<List<OfficeStaff>> getAllStaff(@PathVariable Long teamLeadId) {
            return ResponseEntity.ok(service.getAllStaff(teamLeadId));
        }

        @DeleteMapping("/staff/{staffId}")
        public ResponseEntity<?> deleteStaff(@PathVariable Long staffId) {
            service.deleteStaff(staffId);
            Map<String,String> response=new HashMap<>();
            response.put("message","deleted successfully");
            return ResponseEntity.ok(response);
        }

        // TASK MANAGEMENT
        @PostMapping("/{teamLeadId}/task/assign")
        public ResponseEntity<?> assignTask(
                @PathVariable Long teamLeadId,
                @RequestBody TaskRequest request) {
            return ResponseEntity.ok(service.assignTask(teamLeadId, request));
        }

        @PutMapping("/task/{taskId}")
        public ResponseEntity<?> updateTask(
                @PathVariable Long taskId,
                @RequestBody TaskUpdateRequest request) {
            return ResponseEntity.ok(service.updateTask(taskId, request));
        }

        @DeleteMapping("/task/{taskId}")
        public ResponseEntity<String> deleteTask(@PathVariable Long taskId) {
            service.deleteTask(taskId);
            return ResponseEntity.ok("Task deleted");
        }

        @GetMapping("/{teamLeadId}/tasks")
        public ResponseEntity<List<TaskResponse>> getAllTasksByTeamLead(
                @PathVariable Long teamLeadId) {

            return ResponseEntity.ok(service.getAllTasksByTeamLead(teamLeadId));
        }

        @GetMapping("/tasks/{taskId}")
        public ResponseEntity<TaskResponse> getTask(@PathVariable Long taskId) {
            return ResponseEntity.ok(service.getTaskById(taskId));
        }

        @GetMapping("/tasks/staff/{staffId}")
        public ResponseEntity<List<TaskResponse>> getByStaff(@PathVariable Long staffId) {
            return ResponseEntity.ok(service.getTasksByStaff(staffId));
        }

        @GetMapping("/tasks/status/{status}")
        public ResponseEntity<List<TaskResponse>> getByStatus(@PathVariable TaskStatus status) {
            return ResponseEntity.ok(service.getTasksByStatus(status));
        }

        @GetMapping("/tasks/date-range")
        public ResponseEntity<List<TaskResponse>> getByDateRange(
                @RequestParam LocalDate start,
                @RequestParam LocalDate end) {
            return ResponseEntity.ok(service.getTasksBetweenDates(start, end));
        }

        // REVIEW
        @PostMapping("/{teamLeadId}/task/{taskId}/review")
        public ResponseEntity<?> reviewTask(
                @PathVariable Long teamLeadId,
                @PathVariable Long taskId,
                @RequestBody TaskReviewRequest request) {
            return ResponseEntity.ok(service.reviewTask(taskId, teamLeadId, request));
        }

        @GetMapping("/reviews/pending")
        public ResponseEntity<List<Task>> pendingReviews() {
            return ResponseEntity.ok(service.getPendingReviewTasks());
        }

        @GetMapping("/reviews/task/{taskId}")
        public ResponseEntity<List<TaskReview>> taskReviews(@PathVariable Long taskId) {
            return ResponseEntity.ok(service.getTaskReviews(taskId));
        }

        // COLLEGE STAFF MANAGEMENT

        @PostMapping("/{teamLeadId}/college-staff")
        public ResponseEntity<?> createCollegeStaff(
                @PathVariable Long teamLeadId,
                @RequestBody CollegeStaff collegeStaff) {

            return ResponseEntity.ok(
                    service.createCollegeStaff(teamLeadId, collegeStaff));
        }

        @GetMapping("/{teamLeadId}/college-staff")
        public ResponseEntity<List<CollegeStaff>> getAllCollegeStaff(
                @PathVariable Long teamLeadId) {

            return ResponseEntity.ok(
                    service.getAllCollegeStaff(teamLeadId));
        }

        @GetMapping("/{teamLeadId}/college-staff/{collegeStaffId}")
        public ResponseEntity<CollegeStaff> getCollegeStaff(
                @PathVariable Long teamLeadId,
                @PathVariable Long collegeStaffId) {

            return ResponseEntity.ok(
                    service.getCollegeStaffById(teamLeadId, collegeStaffId));
        }

        @PutMapping("/{teamLeadId}/college-staff/{collegeStaffId}")
        public ResponseEntity<?> updateCollegeStaff(
                @PathVariable Long teamLeadId,
                @PathVariable Long collegeStaffId,
                @RequestBody CollegeStaff collegeStaff) {

            return ResponseEntity.ok(
                    service.updateCollegeStaff(
                            teamLeadId,
                            collegeStaffId,
                            collegeStaff));
        }

        @DeleteMapping("/{teamLeadId}/college-staff/{collegeStaffId}")
        public ResponseEntity<String> deleteCollegeStaff(
                @PathVariable Long teamLeadId,
                @PathVariable Long collegeStaffId) {

            service.deleteCollegeStaff(
                    teamLeadId,
                    collegeStaffId);

            return ResponseEntity.ok(
                    "College Staff Deleted Successfully");
        }

        @GetMapping("/{teamLeadId}/students")
        public ResponseEntity<List<Student>> getAllStudents(
                @PathVariable Long teamLeadId) {

            return ResponseEntity.ok(service.getAllStudents(teamLeadId));
        }

        @GetMapping("/{teamLeadId}/students/{studentId}")
        public ResponseEntity<Student> getStudentById(
                @PathVariable Long teamLeadId,
                @PathVariable Long studentId) {

            return ResponseEntity.ok(service.getStudentById(teamLeadId, studentId));
        }

        @GetMapping("/{teamLeadId}/students/code/{studentId}")
        public ResponseEntity<Student> getByStudentCode(
                @PathVariable Long teamLeadId,
                @PathVariable String studentId) {

            return ResponseEntity.ok(service.getStudentByStudentId(teamLeadId, studentId));
        }

        @GetMapping("/{teamLeadId}/students/email")
        public ResponseEntity<Student> getByEmail(
                @PathVariable Long teamLeadId,
                @RequestParam String email) {

            return ResponseEntity.ok(service.getStudentByEmail(teamLeadId, email));
        }

        // Reviewing staff requests
        @PutMapping("/{teamLeadId}/leave/{leaveId}/review")
        public ResponseEntity<?> reviewStaffLeave(@PathVariable Long teamLeadId, @PathVariable Long leaveId, @RequestParam String status) {
            return ResponseEntity.ok(service.reviewStaffLeave(teamLeadId, leaveId, status));
        }

        // Requesting own leave from Admin
        @PostMapping("/{teamLeadId}/leave-request")
        public ResponseEntity<?> requestLeaveFromAdmin(@PathVariable Long teamLeadId, @RequestBody LeaveRequestDTO request) {
            return ResponseEntity.ok(service.requestLeaveFromAdmin(teamLeadId, request));
        }

        @GetMapping("/office-staff/leave")
        public ResponseEntity<?> getAllOfficeStaffLeaveRequests(){
            return ResponseEntity.ok(service.getAllOfficeStaffLeaves());
        }
        /*
    ===================================
    APPROVE PERMISSION REQUEST
    ===================================
    */
        @PutMapping("/permissions/{permissionId}/approve")
        public ResponseEntity<Map<String, String>> approvePermission(
                @PathVariable Long permissionId) {

            Map<String, String> response = service.approvePermission(permissionId);
            return ResponseEntity.ok(response);
        }

        /*
        ===================================
        REJECT PERMISSION REQUEST
        ===================================
        */
        @PutMapping("/permissions/{permissionId}/reject")
        public ResponseEntity<Map<String, String>> rejectPermission(
                @PathVariable Long permissionId,
                @RequestParam(required = false) String remarks) {

            Map<String, String> response = service.rejectPermission(permissionId, remarks);
            return ResponseEntity.ok(response);
        }

        /*
    ===================================
    TL: FETCH ALL PERMISSIONS WITHIN BRANCH
    ===================================
    */
        @GetMapping("/{leaderStaffId}/permissions")
        public ResponseEntity<List<OfficeStaffPermission>> getAllBranchPermissions(
                @PathVariable Long leaderStaffId) {

            List<OfficeStaffPermission> permissions = service.getAllBranchPermissions(leaderStaffId);
            return ResponseEntity.ok(permissions);
        }

        /*
        ===================================
        TL: FETCH PENDING ACTIONABLE PERMISSIONS
        ===================================
        */
        @GetMapping("/{leaderStaffId}/permissions/pending")
        public ResponseEntity<List<OfficeStaffPermission>> getPendingBranchPermissions(
                @PathVariable Long leaderStaffId) {

            List<OfficeStaffPermission> pendingRequests = service.getPendingBranchPermissions(leaderStaffId);
            return ResponseEntity.ok(pendingRequests);
        }

        /*
    ===================================
    TL: REQUEST OWN PERMISSION FROM ADMIN
    ===================================
    */
        @PostMapping("/{teamLeadId}/permission-request") // Fixed: Changed from /leave-request to prevent conflict
        public ResponseEntity<?> requestPermissionFromAdmin(
                @PathVariable Long teamLeadId,
                @RequestBody TeamLeadPermissionRequestDTO request) {
            return ResponseEntity.ok(service.requestPermissionToAdmin(teamLeadId, request));
        }
    }