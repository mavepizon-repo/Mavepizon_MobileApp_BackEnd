package com.example.MpApp.service.admin;

import com.example.MpApp.dto.task.TaskRequest;
import com.example.MpApp.dto.task.TaskResponse;
import com.example.MpApp.dto.task.TaskReviewRequest;
import com.example.MpApp.dto.task.TaskUpdateRequest;
import com.example.MpApp.dto.teamlead.TeamLeadPermissionRequestDTO;
import com.example.MpApp.entity.admin.Admin;
import com.example.MpApp.entity.collegestaff.CollegeStaff;
import com.example.MpApp.entity.course.Course;
import com.example.MpApp.entity.course.OfferedCourse;
import com.example.MpApp.entity.developer_trainer_staff.BatchStudents;
import com.example.MpApp.entity.developer_trainer_staff.TrainingBatch;
import com.example.MpApp.entity.enums.StaffCategory;
import com.example.MpApp.entity.enums.TaskStatus;
import com.example.MpApp.entity.enums.VerificationStatus;
import com.example.MpApp.entity.officestaff.OfficeStaff;
import com.example.MpApp.entity.student.Student;
import com.example.MpApp.entity.task.Task;
import com.example.MpApp.entity.task.TaskReview;
import com.example.MpApp.entity.task.TaskUpdate;
import com.example.MpApp.entity.teamlead.TeamLead;
import com.example.MpApp.entity.teamlead.TeamLeadLeave;
import com.example.MpApp.entity.teamlead.TeamLeadPermission;
import com.example.MpApp.repository.admin.AdminRepository;
import com.example.MpApp.dto.admin.AdminLoginRequest;
import com.example.MpApp.config.JwtService;
import com.example.MpApp.exception.DuplicateResourceException;
import com.example.MpApp.exception.InvalidCredentialsException;
import com.example.MpApp.exception.ResourceNotFoundException;
import com.example.MpApp.repository.collegestaff.CollegeStaffRepository;
import com.example.MpApp.repository.course.CourseRepository;
import com.example.MpApp.repository.course.OfferedCourseRepository;
import com.example.MpApp.repository.developer_trainer.BatchStudentRepository;
import com.example.MpApp.repository.developer_trainer.TrainingBatchRepository;
import com.example.MpApp.repository.student.StudentRepository;
import com.example.MpApp.repository.task.TaskRepository;
import com.example.MpApp.repository.task.TaskReviewRepository;
import com.example.MpApp.repository.task.TaskUpdateRepository;
import com.example.MpApp.repository.teamlead.TeamLeadLeaveRepository;
import com.example.MpApp.repository.teamlead.TeamLeadPermissionRepository;
import com.example.MpApp.repository.teamlead.TeamLeadRepository;
import com.example.MpApp.repository.officestaff.OfficeStaffRepository;
import com.example.MpApp.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final TeamLeadRepository teamLeadRepository;
    private final TaskRepository taskRepository;
    private final TaskUpdateRepository taskUpdateRepository;
    private final TaskReviewRepository taskReviewRepository;
    private final OfficeStaffRepository officeStaffRepository;
    private final CollegeStaffRepository collegeStaffRepository;
    private final StudentRepository studentRepository;
    private final TeamLeadLeaveRepository teamLeadLeaveRepository;
    private final BCryptPasswordEncoder encoder;
    private final JwtService jwtService;
    private final TeamLeadPermissionRepository teamLeadPermissionRepository;
    private final TrainingBatchRepository batchRepository;
    private final BatchStudentRepository batchStudentRepository;
    private final OfferedCourseRepository offeredCourseRepository;
    private final CourseRepository courseRepository;

    private final CloudinaryService cloudinaryService;

    private String generateTeamLeadId(String branch) {
        String branchCode = switch (branch.trim().toUpperCase()) {
            case "TIRUNELVELI" -> "TVL";
            case "THISAYANVILAI" -> "TSV";
            case "NAGERCOIL" -> "NGL";
            case "COIMBATORE" -> "CBT";
            default -> throw new IllegalArgumentException("Invalid Branch: " + branch);
        };

        String prefix = "MP" + branchCode + "TL";
        long count = teamLeadRepository.countByTeamLeadIdStartingWith(prefix);
        return prefix + String.format("%03d", count + 1);
    }

    private String generateStaffId(String branch, StaffCategory category) {
        String branchCode = switch (branch.toUpperCase()) {
            case "COIMBATORE" -> "CBT";
            case "TIRUNELVELI" -> "TVL";
            case "THISAYANVILAI" -> "TSV";
            case "NAGERCOIL" -> "NGL";
            default -> throw new IllegalArgumentException("Invalid Branch: " + branch);
        };

        String categoryCode = switch (category) {
            case DEVELOPER -> "DE";
            case DEVELOPER_TRAINER -> "DT";
            case TELECOM_SERVICE -> "TE";
            case DESIGNER -> "DS";
            case FREELANCER -> "FL";
            default -> throw new IllegalArgumentException("Invalid Category: " + category);
        };

        long count = officeStaffRepository.count();
        return "MP" + branchCode + categoryCode + String.format("%03d", count + 1);
    }

    // ================= ADMIN AUTH (UPDATED) =================

    @Transactional
    public Map<String, String> registerAdmin(Admin admin) {
        if (adminRepository.findByEmail(admin.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Admin already exists with email: " + admin.getEmail());
        }

        admin.setPassword(encoder.encode(admin.getPassword()));
        Admin savedAdmin = adminRepository.save(admin);

        Map<String, String> response = new HashMap<>();
        response.put("adminId", savedAdmin.getId().toString());
        response.put("message", "Admin registered successfully");
        return response;
    }

    public Admin updateAdminFiles(Long id, MultipartFile profile) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin context not found for ID: " + id));

        // Fix: Route explicitly to the admin/profile subfolder
        if (profile != null && !profile.isEmpty()) {
            admin.setProfile(cloudinaryService.uploadFile(profile, "admin/profile"));
        }

        return adminRepository.save(admin);
    }

    public Map<String, String> login(AdminLoginRequest request) {
        Admin admin = adminRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with email: " + request.getEmail()));

        if (!encoder.matches(request.getPassword(), admin.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        UserDetails userDetails = User.builder()
                .username(admin.getEmail())
                .password(admin.getPassword())
                .roles("ADMIN")
                .build();

        String token = jwtService.generateToken(userDetails);

        Map<String, String> response = new HashMap<>();
        response.put("adminId", admin.getId().toString());
        response.put("email", admin.getEmail());
        response.put("token", token);
        response.put("message", "Login successful");
        return response;
    }

    // ================= JWT VALIDATION =================

    public Admin validateAdminToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidCredentialsException("Missing or invalid token layout");
        }

        String token = authHeader.substring(7);
        String email = jwtService.extractEmail(token);

        return adminRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found or token signature invalid"));
    }

    // ================= TEAM LEAD MANAGEMENT (UPDATED) =================

    @Transactional
    public Map<String, String> createTeamLead(Long adminId, TeamLead teamLead) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin registration context missing. ID: " + adminId));

        if (teamLeadRepository.findByEmail(teamLead.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Team Lead already exists with email: " + teamLead.getEmail());
        }

        teamLead.setPassword(encoder.encode(teamLead.getPassword()));
        teamLead.setTeamLeadId(generateTeamLeadId(teamLead.getBranch()));
        teamLead.setCreatedByAdmin(admin);
        teamLead.setActive(true);

        TeamLead savedLead = teamLeadRepository.save(teamLead);

        Map<String, String> response = new HashMap<>();
        response.put("teamLeadId", savedLead.getId().toString());
        response.put("systemId", savedLead.getTeamLeadId());
        response.put("message", "Team Lead Created Successfully");
        return response;
    }

    public TeamLead updateTeamLeadFiles(Long id, MultipartFile profile, MultipartFile aadhaar, MultipartFile resume) {
        TeamLead teamLead = teamLeadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team Lead context not found for ID: " + id));

        // Fix: Route explicitly to teamLead subfolders
        if (profile != null && !profile.isEmpty()) {
            teamLead.setProfilePhoto(cloudinaryService.uploadFile(profile, "teamLead/profile"));
        }
        if (aadhaar != null && !aadhaar.isEmpty()) { // [Aadhaar Redacted]
            teamLead.setAadhaarFile(cloudinaryService.uploadFile(aadhaar, "teamLead/aadhaar"));
        }
        if (resume != null && !resume.isEmpty()) {
            teamLead.setResumeFile(cloudinaryService.uploadFile(resume, "teamLead/resume"));
        }

        return teamLeadRepository.save(teamLead);
    }

    // UNCHANGED GET METHOD
    public List<TeamLead> getAllTeamLeads() {
        return teamLeadRepository.findAll();
    }

    // UNCHANGED GET METHOD
    public TeamLead getTeamLeadById(Long id) {
        return teamLeadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TeamLead not found for ID: " + id));
    }

    @Transactional
    public Map<String, String> updateTeamLead(Long id, TeamLead request) {
        TeamLead existing = teamLeadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TeamLead targeted for changes not found for ID: " + id));

        if (request.getName() != null) existing.setName(request.getName());
        if (request.getRole() != null) existing.setRole(request.getRole());
        if (request.getMobileNumber() != null) existing.setMobileNumber(request.getMobileNumber());
        if (request.getExperience() != null) existing.setExperience(request.getExperience());
        if (request.getSkills() != null) existing.setSkills(request.getSkills());
        if (request.getActive() != null) existing.setActive(request.getActive());

        teamLeadRepository.save(existing);

        Map<String, String> response = new HashMap<>();
        response.put("teamLeadId", id.toString());
        response.put("message", "Team Lead Updated Successfully");
        return response;
    }

    @Transactional
    public Map<String, String> toggleTeamLeadStatus(Long id, boolean active) {
        TeamLead lead = teamLeadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TeamLead record context missing for ID: " + id));
        lead.setActive(active);
        teamLeadRepository.save(lead);

        Map<String, String> response = new HashMap<>();
        response.put("teamLeadId", id.toString());
        response.put("active", String.valueOf(active));
        response.put("message", "Team Lead Status Flag Toggled Successfully");
        return response;
    }

    public void deleteTeamLead(Long id) {
        if (!teamLeadRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete. TeamLead target configuration missing for ID: " + id);
        }
        teamLeadRepository.deleteById(id);
    }

    // ================= OFFICE STAFF MANAGEMENT (UPDATED) =================

    @Transactional
    public Map<String, String> createStaff(Long adminId, OfficeStaff staff) {
        adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin validation context tracking missing for ID: " + adminId));

        if (officeStaffRepository.findByEmail(staff.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Office staff layout with email context already exists: " + staff.getEmail());
        }

        staff.setStaffId(generateStaffId(staff.getBranch(), staff.getCategory()));
        staff.setPassword(encoder.encode(staff.getPassword()));
        staff.setScore(100);

        OfficeStaff savedStaff = officeStaffRepository.save(staff);

        Map<String, String> response = new HashMap<>();
        response.put("staffId", savedStaff.getId().toString());
        response.put("systemId", savedStaff.getStaffId());
        response.put("message", "Office Staff Created Successfully");
        return response;
    }

    public OfficeStaff updateStaffFiles(Long id, MultipartFile profile, MultipartFile aadhaar, MultipartFile resume) {
        OfficeStaff staff = officeStaffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Office Staff context not found for ID: " + id));

        // Keeps the existing officeStaff directory grouping
        if (profile != null && !profile.isEmpty()) {
            staff.setProfilePhoto(cloudinaryService.uploadFile(profile, "officeStaff/profile"));
        }
        if (aadhaar != null && !aadhaar.isEmpty()) { // [Aadhaar Redacted]
            staff.setAadhaarFile(cloudinaryService.uploadFile(aadhaar, "officeStaff/aadhaar"));
        }
        if (resume != null && !resume.isEmpty()) {
            staff.setResumeFile(cloudinaryService.uploadFile(resume, "officeStaff/resume"));
        }

        return officeStaffRepository.save(staff);
    }

    @Transactional
    public Map<String, String> updateStaff(Long id, OfficeStaff request) {
        OfficeStaff existing = officeStaffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff workplace footprint not found for ID: " + id));

        if (request.getName() != null) existing.setName(request.getName());
        if (request.getRole() != null) existing.setRole(request.getRole());
        if (request.getMobileNumber() != null) existing.setMobileNumber(request.getMobileNumber());
        if (request.getSkills() != null) existing.setSkills(request.getSkills());
        if (request.getBloodGroup() != null) existing.setBloodGroup(request.getBloodGroup());
        if (request.getEmployeeId() != null) existing.setEmployeeId(request.getEmployeeId());

        officeStaffRepository.save(existing);

        Map<String, String> response = new HashMap<>();
        response.put("staffId", id.toString());
        response.put("message", "Office Staff Updated Successfully");
        return response;
    }

    // UNCHANGED GET METHOD
    public List<OfficeStaff> getAllStaff() {
        return officeStaffRepository.findAll();
    }

    // UNCHANGED GET METHOD
    public OfficeStaff getStaffById(Long id) {
        return officeStaffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff footprint not found for ID: " + id));
    }

    public void deleteStaff(Long id) {
        if (!officeStaffRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete. Office Staff instance missing for ID: " + id);
        }
        officeStaffRepository.deleteById(id);
    }

    @Transactional
    public Map<String, String> assignTaskByAdmin(Long adminId, TaskRequest request) {
        // Verify admin exists
        adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found for ID: " + adminId));

        // Retrieve the target staff
        OfficeStaff staff = officeStaffRepository.findById(request.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found for ID: " + request.getStaffId()));

        // Create and save the task
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setAssignedDate(java.time.LocalDate.now());
        task.setDeadline(request.getDeadline());
        task.setTaskType(request.getTaskType());
        task.setPriority(request.getPriority());
        task.setEstimatedHours(request.getEstimatedHours());
        task.setRemarks(request.getRemarks());
        task.setStatus(com.example.MpApp.entity.enums.TaskStatus.ASSIGNED);
        task.setProgress(0);
        task.setStaff(staff);
        // Admin assignment: teamLead can be null or set to a default value depending on your business logic
        task.setTeamLead(null);

        Task savedTask = taskRepository.save(task);

        Map<String, String> response = new HashMap<>();
        response.put("taskId", savedTask.getId().toString());
        response.put("message", "Task Assigned Successfully by Admin");
        return response;
    }

    // ================= TASK MANAGEMENT (ADMIN) =================

    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAllTasks();
    }

    public TaskResponse getTaskById(Long taskId) {
        return taskRepository.findTaskById(taskId);
    }

    public List<TaskResponse> getTasksByStaff(Long staffId) {
        return taskRepository.findTasksByStaff(staffId);
    }

    public List<TaskResponse> getTasksByStatus(TaskStatus status) {
        return taskRepository.findTasksByStatus(status);
    }

    @Transactional
    public void deleteTaskByAdmin(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found for ID: " + taskId);
        }
        taskRepository.deleteById(taskId);
    }

    @Transactional
    public Map<String, String> updateTaskByAdmin(Long taskId, TaskUpdateRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found for ID: " + taskId));

        TaskUpdate update = new TaskUpdate();
        update.setTask(task);
        update.setProgressPercentage(request.getProgressPercentage());
        update.setWorkDoneToday(request.getWorkDoneToday());
        update.setBlockers(request.getBlockers());
        update.setComments(request.getComments());
        update.setAttachmentUrl(request.getAttachmentUrl());
        update.setStatus(request.getStatus());
        update.setUpdatedAt(java.time.LocalDateTime.now());

        taskUpdateRepository.save(update);

        task.setProgress(request.getProgressPercentage());
        task.setStatus(request.getStatus());
        taskRepository.save(task);

        Map<String, String> response = new HashMap<>();
        response.put("taskId", taskId.toString());
        response.put("message", "Task Updated Successfully by Admin");
        return response;
    }

    @Transactional
    public Map<String, String> reviewTaskByAdmin(Long taskId, Long adminId, TaskReviewRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found for ID: " + taskId));

        // Admin doesn't necessarily have a TeamLead entity, so adjust review logic if needed
        // Assuming you want the admin to review it similar to a Team Lead
        TaskReview review = new TaskReview();
        review.setTask(task);
        // If your TaskReview requires a TeamLead, you may need to update your entity
        // to support Admin reviews or just leave it null/generic
        review.setVerificationStatus(request.getVerificationStatus());
        review.setReviewComment(request.getReviewComment());
        review.setReviewedAt(java.time.LocalDateTime.now());

        task.setStatus(request.getVerificationStatus() == VerificationStatus.APPROVED ?
                TaskStatus.COMPLETED : TaskStatus.REWORK_REQUIRED);

        taskRepository.save(task);
        taskReviewRepository.save(review);

        return Map.of("message", "Task Reviewed by Admin successfully");
    }

    // ================= COLLEGE STAFF MANAGEMENT (UPDATED) =================

    @Transactional
    public Map<String, String> createCollegeStaff(CollegeStaff staff) {
        if (collegeStaffRepository.existsByEmail(staff.getEmail())) {
            throw new DuplicateResourceException("College staff email footprint already registered: " + staff.getEmail());
        }
        if (collegeStaffRepository.existsByMobileNumber(staff.getMobileNumber())) {
            throw new DuplicateResourceException("College staff mobile layout sequence already registered: " + staff.getMobileNumber());
        }

        staff.setPassword(encoder.encode(staff.getPassword()));
        CollegeStaff savedStaff = collegeStaffRepository.save(staff);

        Map<String, String> response = new HashMap<>();
        response.put("collegeStaffId", savedStaff.getId().toString());
        response.put("message", "College Staff Created Successfully");
        return response;
    }

    @Transactional
    public Map<String, String> updateCollegeStaff(Long id, CollegeStaff request) {
        CollegeStaff existing = collegeStaffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("College Staff target mapping not found for ID: " + id));

        if (request.getName() != null) existing.setName(request.getName());
        if (request.getCollegeName() != null) existing.setCollegeName(request.getCollegeName());
        if (request.getDepartment() != null) existing.setDepartment(request.getDepartment());
        if (request.getEmail() != null) existing.setEmail(request.getEmail());
        if (request.getMobileNumber() != null) existing.setMobileNumber(request.getMobileNumber());
        if (request.getGender() != null) existing.setGender(request.getGender());

        collegeStaffRepository.save(existing);

        Map<String, String> response = new HashMap<>();
        response.put("collegeStaffId", id.toString());
        response.put("message", "College Staff Details Updated Successfully");
        return response;
    }

    // UNCHANGED GET METHOD
    public List<CollegeStaff> getAllCollegeStaff() {
        return collegeStaffRepository.findAll();
    }

    // UNCHANGED GET METHOD
    public CollegeStaff getCollegeStaffById(Long id) {
        return collegeStaffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("College Staff workspace metadata not found for ID: " + id));
    }

    public void deleteCollegeStaff(Long id) {
        if (!collegeStaffRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete. College Staff mapping details missing for ID: " + id);
        }
        collegeStaffRepository.deleteById(id);
    }

    // ================= UNCHANGED STUDENT GET METHODS =================

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student trace index not found for ID: " + id));
    }

    public Student getStudentByEmail(String email) {
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student target query matching email returned empty: " + email));
    }

    public Student getStudentByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student system registration ID code context not found: " + studentId));
    }

    // ================= LEAVE MANAGEMENT (UPDATED) =================

    @Transactional
    public Map<String, String> reviewTeamLeadLeave(Long adminId, Long leaveId, String status) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin validation signature record missing for ID: " + adminId));

        TeamLeadLeave leave = teamLeadLeaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead leave booking voucher context not found for ID: " + leaveId));

        leave.setStatus(status);
        leave.setApprovedBy(admin);
        teamLeadLeaveRepository.save(leave);

        Map<String, String> response = new HashMap<>();
        response.put("leaveId", leaveId.toString());
        response.put("status", status);
        response.put("message", "Team Lead Leave Request Evaluated Successfully");
        return response;
    }

    // UNCHANGED GET METHOD
    public List<TeamLeadLeave> getAllTeamLeadLeaveRequests() {
        return teamLeadLeaveRepository.findAll();
    }

    @Transactional
    public Map<String, String> approveTeamLeadPermission(Long permissionId) {
        TeamLeadPermission permission = teamLeadPermissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission record not found for ID: " + permissionId));

        if (!"PENDING".equalsIgnoreCase(permission.getStatus())) {
            throw new IllegalStateException("Permission has already been processed.");
        }

        // Double check quota limit during actual approval transaction
        long approvedCount = teamLeadPermissionRepository.countApprovedPermissionsByLeadAndMonth(
                permission.getTeamLead().getId(),
                permission.getPermissionDate().getMonthValue(),
                permission.getPermissionDate().getYear()
        );

        if (approvedCount >= 2) {
            throw new IllegalStateException("Cannot approve. Team Lead has already utilized their 2 approved monthly permissions.");
        }

        permission.setStatus("APPROVED");
        teamLeadPermissionRepository.save(permission);

        Map<String, String> response = new HashMap<>();
        response.put("permissionId", permissionId.toString());
        response.put("status", "APPROVED");
        response.put("message", "Team Lead permission approved successfully.");
        return response;
    }

    @Transactional
    public Map<String, String> rejectTeamLeadPermission(Long permissionId, String remarks) {
        TeamLeadPermission permission = teamLeadPermissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission record not found for ID: " + permissionId));

        if (!"PENDING".equalsIgnoreCase(permission.getStatus())) {
            throw new IllegalStateException("Permission has already been processed.");
        }

        permission.setStatus("REJECTED");
        permission.setRemarks(remarks != null ? remarks : "Rejected by Admin");
        teamLeadPermissionRepository.save(permission);

        Map<String, String> response = new HashMap<>();
        response.put("permissionId", permissionId.toString());
        response.put("status", "REJECTED");
        response.put("message", "Team Lead permission rejected successfully.");
        return response;
    }

    public List<TeamLeadPermission> getPendingAdminPermissions() {
        return teamLeadPermissionRepository.findByStatusIgnoreCase("PENDING");
    }
    // Add this map at the class level of AdminService
    private final Map<String, String> otpStorage = new HashMap<>();

    public String sendOtp(String email) {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email Not Found"));

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        otpStorage.put(email, otp);
        System.out.println("OTP for Admin (" + email + ") is: " + otp);
        return "OTP sent successfully";
    }

    public String verifyOtp(String email, String otp) {
        if (!otpStorage.containsKey(email)) {
            throw new RuntimeException("OTP not requested");
        }
        if (!otpStorage.get(email).equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }
        return "OTP Verified Successfully";
    }

    public String resetPassword(String email, String otp, String newPassword) {
        if (!otpStorage.containsKey(email)) {
            throw new RuntimeException("OTP not requested");
        }
        if (!otpStorage.get(email).equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email Not Found"));

        admin.setPassword(encoder.encode(newPassword));
        adminRepository.save(admin);
        otpStorage.remove(email);
        return "Password Reset Successful";
    }

    public Map<String, Object> getAdminDashboardStats() {
        // Call the class's own methods directly
        long branchStaffCount = getAllStaff().size();
        long teamLeadCount = getAllTeamLeads().size();

        Map<String, Object> statistics = new java.util.HashMap<>();
        statistics.put("totalStaff", branchStaffCount);
        statistics.put("totalTeamLeads", teamLeadCount);
        statistics.put("systemStatus", "OPERATIONAL");
        return statistics;
    }

    // ================= TRAINING BATCH CREATION (ADMIN) =================

    @Transactional
    public Map<String, String> createTrainingBatch(Long adminId, TrainingBatch batch, Long courseId, Long offeredCourseId) {
        adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin validation context missing for ID: " + adminId));

        // Link Course if provided
        if (courseId != null) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found for ID: " + courseId));
            batch.setCourse(course);
        }

        // Link OfferedCourse (Batch/Offering) if provided
        if (offeredCourseId != null) {
            OfferedCourse offeredCourse = offeredCourseRepository.findById(offeredCourseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Offered Course not found for ID: " + offeredCourseId));
            batch.setOfferedCourse(offeredCourse);
        }

        TrainingBatch savedBatch = batchRepository.save(batch);

        Map<String, String> response = new HashMap<>();
        response.put("batchId", savedBatch.getId().toString());
        response.put("batchName", savedBatch.getBatchName());
        response.put("message", "Training Batch Created Successfully with Course/Offering links by Admin");
        return response;
    }

    @Transactional
    public Map<String, String> assignStaffToBatch(Long batchId, Long staffId) {
        TrainingBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Training batch not found for ID: " + batchId));

        OfficeStaff staff = officeStaffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Office staff not found for ID: " + staffId));

        // Update the single trainer association defined in your TrainingBatch entity
        batch.setTrainer(staff);
        batchRepository.save(batch);

        return Map.of(
                "status", "SUCCESS",
                "message", String.format("Staff member '%s' successfully assigned as Trainer for Batch '%s'",
                        staff.getName(), batch.getBatchName())
        );
    }

    /**
     * Updates a batch assignment by checking a bulk list of IDs and setting the final valid staff element.
     */
    @Transactional
    public Map<String, String> assignStaffToBatchBulk(Long batchId, List<Long> staffIds) {
        if (staffIds == null || staffIds.isEmpty()) {
            throw new IllegalArgumentException("Staff ID selection list cannot be empty.");
        }

        TrainingBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Training batch not found for ID: " + batchId));

        // Resolve the collection list of staff members from the DB
        List<OfficeStaff> staffList = officeStaffRepository.findAllById(staffIds);
        if (staffList.isEmpty()) {
            throw new ResourceNotFoundException("No valid office staff records found for the provided IDs.");
        }

        // Assign the primary/first candidate from the validated bulk collection list
        OfficeStaff primaryStaff = staffList.get(0);
        batch.setTrainer(primaryStaff);
        batchRepository.save(batch);

        return Map.of(
                "status", "SUCCESS",
                "message", String.format("Bulk evaluation complete. Primary Trainer '%s' assigned to Batch ID %d (Total validated: %d)",
                        primaryStaff.getName(), batchId, staffList.size())
        );
    }

    @Transactional
    public Map<String, String> assignStudentsToBatch(Long batchId, List<Long> studentIds) {

        if (studentIds == null || studentIds.isEmpty()) {
            throw new IllegalArgumentException("Student ID list cannot be empty.");
        }

        // 1. Resolve training batch
        TrainingBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Training batch not found for ID: " + batchId));

        // 2. Process student enrollments
        List<BatchStudents> newEnrollments = new ArrayList<>();
        int successfullyAdded = 0;
        int skippedDuplicates = 0;

        for (Long studentId : studentIds) {
            // Check for duplicates to prevent DB constraint violations
            if (batchStudentRepository.existsByBatchIdAndStudentId(batchId, studentId)) {
                skippedDuplicates++;
                continue;
            }

            // Fetch the student
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found for ID: " + studentId));

            // Create the enrollment mapping
            BatchStudents enrollment = new BatchStudents();
            enrollment.setBatch(batch);
            enrollment.setStudent(student);
            enrollment.setEnrolledDate(java.time.LocalDate.now());

            newEnrollments.add(enrollment);
            successfullyAdded++;
        }

        // 3. Bulk save valid enrollments
        if (!newEnrollments.isEmpty()) {
            batchStudentRepository.saveAll(newEnrollments);
        }

        return Map.of(
                "status", "SUCCESS",
                "message", "Batch student assignment complete.",
                "studentsAdded", String.valueOf(successfullyAdded),
                "skippedDuplicates", String.valueOf(skippedDuplicates),
                "batchName", batch.getBatchName()
        );
    }
}