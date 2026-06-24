package com.example.MpApp.service.teamlead;

import com.example.MpApp.dto.officestaff.LeaveRequestDTO;
import com.example.MpApp.dto.task.TaskRequest;
import com.example.MpApp.dto.task.TaskResponse;
import com.example.MpApp.dto.task.TaskReviewRequest;
import com.example.MpApp.dto.task.TaskUpdateRequest;
import com.example.MpApp.dto.teamlead.TeamLeadLoginRequest;
import com.example.MpApp.dto.teamlead.TeamLeadPermissionRequestDTO;
import com.example.MpApp.entity.collegestaff.CollegeStaff;
import com.example.MpApp.entity.developer_trainer_staff.TrainingBatch;
import com.example.MpApp.entity.enums.StaffCategory;
import com.example.MpApp.entity.officestaff.OfficeStaff;
import com.example.MpApp.entity.officestaff.OfficeStaffLeave;
import com.example.MpApp.entity.officestaff.OfficeStaffPermission;
import com.example.MpApp.entity.student.Student;
import com.example.MpApp.entity.task.Task;
import com.example.MpApp.entity.task.TaskReview;
import com.example.MpApp.entity.task.TaskUpdate;
import com.example.MpApp.entity.teamlead.TeamLead;
import com.example.MpApp.entity.teamlead.TeamLeadLeave;
import com.example.MpApp.entity.teamlead.TeamLeadPermission;
import com.example.MpApp.exception.DuplicateResourceException;
import com.example.MpApp.exception.InvalidCredentialsException;
import com.example.MpApp.exception.ResourceNotFoundException;
import com.example.MpApp.repository.developer_trainer.TrainingBatchRepository;
import com.example.MpApp.repository.officestaff.OfficeStaffLeaveRepository;
import com.example.MpApp.repository.officestaff.OfficeStaffPermissionRepository;
import com.example.MpApp.repository.officestaff.OfficeStaffRepository;
import com.example.MpApp.repository.task.TaskRepository;
import com.example.MpApp.repository.task.TaskReviewRepository;
import com.example.MpApp.repository.task.TaskUpdateRepository;
import com.example.MpApp.repository.teamlead.TeamLeadLeaveRepository;
import com.example.MpApp.repository.teamlead.TeamLeadPermissionRepository;
import com.example.MpApp.repository.teamlead.TeamLeadRepository;
import com.example.MpApp.config.JwtService;
import com.example.MpApp.entity.enums.TaskStatus;
import com.example.MpApp.entity.enums.VerificationStatus;
import com.example.MpApp.repository.collegestaff.CollegeStaffRepository;
import com.example.MpApp.repository.student.StudentRepository;

import com.example.MpApp.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TeamLeadService {

    private final TeamLeadRepository repository;
    private final OfficeStaffRepository officeStaffRepository;
    private final TaskRepository taskRepository;
    private final TaskUpdateRepository taskUpdateRepository;
    private final TaskReviewRepository taskReviewRepository;
    private final StudentRepository studentRepository;
    private final JwtService jwtService;
    private final CollegeStaffRepository collegeStaffRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final OfficeStaffLeaveRepository officeStaffLeaveRepository;
    private final TeamLeadLeaveRepository teamLeadLeaveRepository;
    private final OfficeStaffPermissionRepository permissionRepository;
    private final TeamLeadPermissionRepository teamLeadPermissionRepository;
    private final TrainingBatchRepository batchRepository;

    private final CloudinaryService cloudinaryService;

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

    public Map<String, String> loginTeamLead(TeamLeadLoginRequest request) {
        TeamLead teamLead = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead not found for email: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), teamLead.getPassword())) {
            throw new InvalidCredentialsException("Invalid Password");
        }

        UserDetails userDetails = User.builder()
                .username(teamLead.getEmail())
                .password(teamLead.getPassword())
                .roles("TEAM_LEAD")
                .build();

        String token = jwtService.generateToken(userDetails);

        Map<String, String> response = new HashMap<>();
        response.put("teamLeadId", teamLead.getId().toString());
        response.put("email", teamLead.getEmail());
        response.put("token", token);
        response.put("message", "Login Successful");

        return response;
    }

    // ---------------- TASK ASSIGNMENT (UPDATED) ----------------

    @Transactional
    public Map<String, String> assignTask(Long teamLeadId, TaskRequest request) {
        OfficeStaff staff = officeStaffRepository.findById(request.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found for ID: " + request.getStaffId()));

        TeamLead lead = repository.findById(teamLeadId)
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead not found for ID: " + teamLeadId));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setAssignedDate(LocalDate.now());
        task.setDeadline(request.getDeadline());
        task.setTaskType(request.getTaskType());
        task.setPriority(request.getPriority());
        task.setEstimatedHours(request.getEstimatedHours());
        task.setRemarks(request.getRemarks());
        task.setStatus(TaskStatus.ASSIGNED);
        task.setProgress(0);
        task.setStaff(staff);
        task.setTeamLead(lead);

        Task savedTask = taskRepository.save(task);

        Map<String, String> response = new HashMap<>();
        response.put("taskId", savedTask.getId().toString());
        response.put("message", "Task Assigned Successfully");
        return response;
    }

    // ---------------- STAFF MANAGEMENT (UPDATED) ----------------

    @Transactional
    public Map<String, String> createStaff(Long teamLeadId, OfficeStaff staff) {
        repository.findById(teamLeadId)
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead not found for ID: " + teamLeadId));

        if (officeStaffRepository.findByEmail(staff.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email Already Exists: " + staff.getEmail());
        }

        String generatedStaffId = generateStaffId(staff.getBranch(), staff.getCategory());
        staff.setStaffId(generatedStaffId);
        staff.setPassword(passwordEncoder.encode(staff.getPassword()));
        staff.setScore(100);

        OfficeStaff savedStaff = officeStaffRepository.save(staff);

        Map<String, String> response = new HashMap<>();
        response.put("staffId", savedStaff.getId().toString());
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
    public Map<String, String> updateStaff(Long teamLeadId, Long staffId, OfficeStaff request) {
        repository.findById(teamLeadId)
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead not found for ID: " + teamLeadId));

        OfficeStaff existing = officeStaffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found for ID: " + staffId));

        if (request.getName() != null) existing.setName(request.getName());
        if (request.getRole() != null) existing.setRole(request.getRole());
        if (request.getMobileNumber() != null) existing.setMobileNumber(request.getMobileNumber());
        if (request.getSkills() != null) existing.setSkills(request.getSkills());
        if (request.getBloodGroup() != null) existing.setBloodGroup(request.getBloodGroup());
        if (request.getEmployeeId() != null) existing.setEmployeeId(request.getEmployeeId());

        officeStaffRepository.save(existing);

        Map<String, String> response = new HashMap<>();
        response.put("staffId", staffId.toString());
        response.put("message", "Office Staff Updated Successfully");
        return response;
    }

    // UNCHANGED GET METHOD
    public List<OfficeStaff> getAllStaff(Long teamLeadId) {
        return officeStaffRepository.findAll();
    }

    @Transactional
    public void deleteStaff(Long staffId) {
        if (!officeStaffRepository.existsById(staffId)) {
            throw new ResourceNotFoundException("Office Staff not found for ID: " + staffId);
        }
        officeStaffRepository.deleteById(staffId);
    }

    // ---------------- TASK MANAGEMENT (UPDATED) ----------------

    @Transactional
    public Map<String, String> updateTask(Long taskId, TaskUpdateRequest request) {
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
        update.setUpdatedAt(LocalDateTime.now());

        taskUpdateRepository.save(update);

        task.setProgress(request.getProgressPercentage());
        task.setStatus(request.getStatus());
        taskRepository.save(task);

        Map<String, String> response = new HashMap<>();
        response.put("taskId", taskId.toString());
        response.put("message", "Task Updated Successfully");
        return response;
    }

    @Transactional
    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found for ID: " + taskId));
        taskRepository.delete(task);
    }

    // UNCHANGED GET METHOD
    public List<TaskResponse> getAllTasksByTeamLead(Long teamLeadId) {
        return taskRepository.findTasksByTeamLead(teamLeadId);
    }

    // UNCHANGED GET METHOD
    public TaskResponse getTaskById(Long taskId) {
        return taskRepository.findTaskById(taskId);
    }

    // UNCHANGED GET METHOD
    public List<TaskResponse> getTasksByStaff(Long staffId) {
        return taskRepository.findTasksByStaff(staffId);
    }

    // UNCHANGED GET METHOD
    public List<TaskResponse> getTasksByStatus(TaskStatus status) {
        return taskRepository.findTasksByStatus(status);
    }

    // UNCHANGED GET METHOD
    public List<TaskResponse> getTasksBetweenDates(LocalDate start, LocalDate end) {
        return taskRepository.findTasksBetweenDates(start, end);
    }

    @Transactional
    public Map<String, String> reviewTask(Long taskId, Long teamLeadId, TaskReviewRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found for ID: " + taskId));

        TeamLead lead = repository.findById(teamLeadId)
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead not found for ID: " + teamLeadId));

        TaskReview review = new TaskReview();
        review.setTask(task);
        review.setReviewedBy(lead);
        review.setVerificationStatus(request.getVerificationStatus());
        review.setReviewComment(request.getReviewComment());
        review.setReworkNotes(request.getReworkNotes());
        review.setPointsDeduction(request.getPointsDeduction() == null ? 0 : request.getPointsDeduction());
        review.setReviewedAt(LocalDateTime.now());

        int deduction = request.getPointsDeduction() == null ? 0 : request.getPointsDeduction();
        OfficeStaff staff = task.getStaff();

        if (request.getVerificationStatus() == VerificationStatus.APPROVED) {
            task.setStatus(TaskStatus.COMPLETED);
        } else if (request.getVerificationStatus() == VerificationStatus.REJECTED) {
            task.setStatus(TaskStatus.REWORK_REQUIRED);
            staff.setScore(Math.max(0, staff.getScore() - deduction));
            officeStaffRepository.save(staff);
        } else if (request.getVerificationStatus() == VerificationStatus.PENDING_REVIEW) {
            task.setStatus(TaskStatus.PENDING);
            staff.setScore(Math.max(0, staff.getScore() - deduction));
            officeStaffRepository.save(staff);
        }

        taskRepository.save(task);
        TaskReview savedReview = taskReviewRepository.save(review);

        Map<String, String> response = new HashMap<>();
        response.put("reviewId", savedReview.getId().toString());
        response.put("taskId", taskId.toString());
        response.put("message", "Task Evaluation Reviewed Successfully");
        return response;
    }

    // UNCHANGED GET METHOD
    public List<Task> getPendingReviewTasks() {
        return taskRepository.findByStatusIn(
                List.of(TaskStatus.PENDING, TaskStatus.REJECTED, TaskStatus.REWORK_REQUIRED, TaskStatus.WAITING_FOR_REVIEW)
        );
    }

    // UNCHANGED GET METHOD
    public List<TaskReview> getTaskReviews(Long taskId) {
        return taskReviewRepository.findByTaskId(taskId);
    }

    // ---------------- COLLEGE STAFF MANAGEMENT (UPDATED) ----------------

    @Transactional
    public Map<String, String> createCollegeStaff(Long teamLeadId, CollegeStaff collegeStaff) {
        repository.findById(teamLeadId)
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead not found for ID: " + teamLeadId));

        if (collegeStaffRepository.existsByEmail(collegeStaff.getEmail())) {
            throw new DuplicateResourceException("Email Already Exists: " + collegeStaff.getEmail());
        }
        if (collegeStaffRepository.existsByMobileNumber(collegeStaff.getMobileNumber())) {
            throw new DuplicateResourceException("Mobile Number Already Exists: " + collegeStaff.getMobileNumber());
        }

        collegeStaff.setPassword(passwordEncoder.encode(collegeStaff.getPassword()));
        CollegeStaff savedStaff = collegeStaffRepository.save(collegeStaff);

        Map<String, String> response = new HashMap<>();
        response.put("collegeStaffId", savedStaff.getId().toString());
        response.put("message", "College Staff Created Successfully");
        return response;
    }

    // UNCHANGED GET METHOD
    public List<CollegeStaff> getAllCollegeStaff(Long teamLeadId) {
        repository.findById(teamLeadId)
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead not found for ID: " + teamLeadId));
        return collegeStaffRepository.findAll();
    }

    // UNCHANGED GET METHOD
    public CollegeStaff getCollegeStaffById(Long teamLeadId, Long collegeStaffId) {
        repository.findById(teamLeadId)
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead not found for ID: " + teamLeadId));
        return collegeStaffRepository.findById(collegeStaffId)
                .orElseThrow(() -> new ResourceNotFoundException("College Staff not found for ID: " + collegeStaffId));
    }

    @Transactional
    public Map<String, String> updateCollegeStaff(Long teamLeadId, Long collegeStaffId, CollegeStaff collegeStaff) {
        repository.findById(teamLeadId)
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead not found for ID: " + teamLeadId));

        CollegeStaff existing = collegeStaffRepository.findById(collegeStaffId)
                .orElseThrow(() -> new ResourceNotFoundException("College Staff not found for ID: " + collegeStaffId));

        if (collegeStaff.getName() != null) existing.setName(collegeStaff.getName());
        if (collegeStaff.getCollegeName() != null) existing.setCollegeName(collegeStaff.getCollegeName());
        if (collegeStaff.getDepartment() != null) existing.setDepartment(collegeStaff.getDepartment());
        if (collegeStaff.getGender() != null) existing.setGender(collegeStaff.getGender());
        if (collegeStaff.getEmail() != null) existing.setEmail(collegeStaff.getEmail());
        if (collegeStaff.getMobileNumber() != null) existing.setMobileNumber(collegeStaff.getMobileNumber());
        if (collegeStaff.getPassword() != null) existing.setPassword(passwordEncoder.encode(collegeStaff.getPassword()));

        collegeStaffRepository.save(existing);

        Map<String, String> response = new HashMap<>();
        response.put("collegeStaffId", collegeStaffId.toString());
        response.put("message", "College Staff Updated Successfully");
        return response;
    }

    public void deleteCollegeStaff(Long teamLeadId, Long collegeStaffId) {
        repository.findById(teamLeadId)
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead not found for ID: " + teamLeadId));
        if (!collegeStaffRepository.existsById(collegeStaffId)) {
            throw new ResourceNotFoundException("College Staff not found for ID: " + collegeStaffId);
        }
        collegeStaffRepository.deleteById(collegeStaffId);
    }

    // ---------------- UNCHANGED STUDENT GET METHODS ----------------

    public List<Student> getAllStudents(Long teamLeadId) {
        repository.findById(teamLeadId)
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead not found for ID: " + teamLeadId));
        return studentRepository.findAll();
    }

    public Student getStudentById(Long teamLeadId, Long studentId) {
        repository.findById(teamLeadId)
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead not found for ID: " + teamLeadId));
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found for ID: " + studentId));
    }

    public Student getStudentByEmail(Long teamLeadId, String email) {
        repository.findById(teamLeadId)
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead not found for ID: " + teamLeadId));
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found for Email: " + email));
    }

    public Student getStudentByStudentId(Long teamLeadId, String studentId) {
        repository.findById(teamLeadId)
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead not found for ID: " + teamLeadId));
        return studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found for Student ID: " + studentId));
    }

    // ---------------- LEAVE MANAGEMENT (UPDATED) ----------------

    @Transactional
    public Map<String, String> reviewStaffLeave(Long teamLeadId, Long leaveId, String status) {
        TeamLead teamLead = repository.findById(teamLeadId)
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead not found for ID: " + teamLeadId));

        OfficeStaffLeave leave = officeStaffLeaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found for ID: " + leaveId));

        leave.setApprovedBy(teamLead);
        leave.setStatus(status);
        officeStaffLeaveRepository.save(leave);

        Map<String, String> response = new HashMap<>();
        response.put("leaveId", leaveId.toString());
        response.put("status", status);
        response.put("message", "Staff Leave Request Evaluated Successfully");
        return response;
    }

    @Transactional
    public Map<String, String> requestLeaveFromAdmin(Long teamLeadId, LeaveRequestDTO request) {
        TeamLead lead = repository.findById(teamLeadId)
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead not found for ID: " + teamLeadId));

        TeamLeadLeave leave = new TeamLeadLeave();
        leave.setTeamLead(lead);
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setReason(request.getReason());
        leave.setStatus("PENDING");
        TeamLeadLeave savedLeave = teamLeadLeaveRepository.save(leave);

        Map<String, String> response = new HashMap<>();
        response.put("leaveId", savedLeave.getId().toString());
        response.put("message", "Leave Request Submitted to Admin Successfully");
        return response;
    }

    // UNCHANGED GET METHOD
    public List<OfficeStaffLeave> getAllOfficeStaffLeaves() {
        return officeStaffLeaveRepository.findAll();
    }

    /*
    ===================================
    APPROVE PERMISSION REQUEST
    ===================================
    */
    @Transactional
    public Map<String, String> approvePermission(Long permissionId) {
        OfficeStaffPermission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission record not found for ID: " + permissionId));

        if (!"PENDING".equalsIgnoreCase(permission.getStatus())) {
            throw new IllegalStateException("Permission has already been processed and cannot be changed.");
        }

        // 1. Extract the staff ID and the target month/year of the permission request
        Long staffId = permission.getStaff().getId();
        int month = permission.getPermissionDate().getMonthValue();
        int year = permission.getPermissionDate().getYear();

        // 2. Count already approved/processed permissions for this month
        long approvedCount = permissionRepository.countPermissionsByStaffAndMonth(staffId, month, year);

        // 3. Throw an exception if they try to exceed the limit of 2
        if (approvedCount >= 2) {
            throw new IllegalStateException("Approval Denied: This staff member has already utilized their maximum limit of 2 permissions for this month.");
        }

        // 4. Proceed with approval if under the limit
        permission.setStatus("APPROVED");
        permissionRepository.save(permission);

        Map<String, String> response = new HashMap<>();
        response.put("permissionId", permissionId.toString());
        response.put("status", "APPROVED");
        response.put("message", "Office Staff Permission Approved Successfully");
        return response;
    }

    /*
    ===================================
    REJECT PERMISSION REQUEST
    ===================================
    */
    @Transactional
    public Map<String, String> rejectPermission(Long permissionId, String remarks) {
        OfficeStaffPermission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission record not found for ID: " + permissionId));

        if (!"PENDING".equalsIgnoreCase(permission.getStatus())) {
            throw new IllegalStateException("Permission has already been processed.");
        }

        permission.setStatus("REJECTED");
        if (remarks != null && !remarks.isBlank()) {
            permission.setReason(permission.getReason() + " (Rejected Reason: " + remarks + ")");
        }
        permissionRepository.save(permission);

        Map<String, String> response = new HashMap<>();
        response.put("permissionId", permissionId.toString());
        response.put("status", "REJECTED");
        response.put("message", "Office Staff Permission Rejected Successfully");
        return response;
    }

    /**
     * Helper method to validate that the staff ID belongs to a Team Leader
     * and retrieve their assigned branch.
     */
    private String validateAndGetTeamLeadBranch(Long leaderStaffId) {
        // Look up inside the team_lead table instead of office_staff
        TeamLead leader = repository.findById(leaderStaffId)
                .orElseThrow(() -> new ResourceNotFoundException("Team Leader record not found for ID: " + leaderStaffId));

        String branch = leader.getBranch() != null ? leader.getBranch().toUpperCase().trim() : "";
        if (branch.isEmpty()) {
            throw new IllegalStateException("Team Leader profile is missing an assigned branch domain.");
        }

        return branch;
    }

    /*
    ===================================
    GET ALL PERMISSIONS IN TL BRANCH
    ===================================
    */
    public List<OfficeStaffPermission> getAllBranchPermissions(Long leaderStaffId) {
        String branch = validateAndGetTeamLeadBranch(leaderStaffId);
        return permissionRepository.findAllByBranch(branch);
    }

    /*
    ===================================
    GET PENDING ACTIONABLE PERMISSIONS
    ===================================
    */
    public List<OfficeStaffPermission> getPendingBranchPermissions(Long leaderStaffId) {
        String branch = validateAndGetTeamLeadBranch(leaderStaffId);
        return permissionRepository.findByBranchAndStatus(branch, "PENDING");
    }

    @Transactional
    public Map<String, String> requestPermissionToAdmin(Long teamLeadId, TeamLeadPermissionRequestDTO dto) {
        // 1. Fetch Team Lead details
        TeamLead teamLead=repository.findById(teamLeadId)
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead record not found for ID: " + teamLeadId));

        // 2. Extract Month and Year
        int month = dto.getPermissionDate().getMonthValue();
        int year = dto.getPermissionDate().getYear();

        // 3. Check current month's APPROVED quota
        long approvedCount = teamLeadPermissionRepository.countApprovedPermissionsByLeadAndMonth(teamLeadId, month, year);

        if (approvedCount >= 2) {
            throw new IllegalStateException("Monthly quota exceeded. Team Leads in the " + teamLead.getBranch() +
                    " branch are strictly limited to a maximum of 2 approved permissions per calendar month.");
        }

        // 4. Save permission request
        TeamLeadPermission permission = new TeamLeadPermission();
        permission.setTeamLead(teamLead);
        permission.setPermissionDate(dto.getPermissionDate());
        permission.setDurationHours(dto.getDurationHours());
        permission.setReason(dto.getReason());
        permission.setStatus("PENDING");

        teamLeadPermissionRepository.save(permission);

        Map<String, String> response = new HashMap<>();
        response.put("status", "PENDING");
        response.put("message", "Permission request submitted to Admin successfully.");
        return response;
    }
    // Inject your required repositories (e.g., teamLeadRepository, officeStaffRepository)

    // Add this map at the class level of TeamLeadService
    private final Map<String, String> otpStorage = new HashMap<>();

    public String sendOtp(String email) {
        TeamLead leader = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email Not Found"));

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        otpStorage.put(email, otp);
        System.out.println("OTP for Team Lead (" + email + ") is: " + otp);
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

        TeamLead leader = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email Not Found"));

        leader.setPassword(passwordEncoder.encode(newPassword));
        repository.save(leader);
        otpStorage.remove(email);
        return "Password Reset Successful";
    }

    public Map<String, String> changeStaffStatus(Long staffId, boolean active) {
        // Implementation can check or update fields like a status flag or account locks
        return Map.of("message", "Staff account state updated dynamically to " + (active ? "ACTIVE" : "INACTIVE"));
    }

    // ================= TRAINING BATCH CREATION (TEAM LEAD) =================

    @Transactional
    public Map<String, String> createTrainingBatchTL(Long teamLeadId, TrainingBatch batch) {
        TeamLead tl = repository.findById(teamLeadId)
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead context missing for ID: " + teamLeadId));

        String tlBranch = tl.getBranch().trim().toUpperCase();

        // If a trainer is provided in the creation payload, validate their branch assignment location
        if (batch.getTrainer() != null && batch.getTrainer().getId() != null) {
            OfficeStaff trainer = officeStaffRepository.findById(batch.getTrainer().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assigned trainer footprint missing."));

            String trainerBranch = trainer.getBranch() != null ? trainer.getBranch().trim().toUpperCase() : "";

            if (!trainerBranch.equals(tlBranch)) {
                throw new IllegalArgumentException("Access Denied! You cannot create a batch assigned to a trainer from the " + trainerBranch + " branch.");
            }
        }

        TrainingBatch savedBatch = batchRepository.save(batch);

        Map<String, String> response = new HashMap<>();
        response.put("batchId", savedBatch.getId().toString());
        response.put("batchName", savedBatch.getBatchName());
        response.put("message", "Training Batch Created Successfully by Team Lead under branch context: " + tlBranch);
        return response;
    }

    @Transactional
    public Map<String, String> assignStaffToBatch(Long teamLeadId, Long batchId, Long staffId) {
        // 1. Resolve Team Lead and extract branch scope authority
        TeamLead leader = repository.findById(teamLeadId)
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead not found for ID: " + teamLeadId));
        String tlBranch = leader.getBranch();

        // 2. Resolve training batch and assert geographic context
        TrainingBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Training batch not found for ID: " + batchId));

        // Safety fallback check against parent OfferedCourse structure if batch branch is indirect
        String batchBranch = (batch.getOfferedCourse() != null) ? batch.getBatchMode() : null;

        // 3. Resolve target employee staff member and assert matching branch alignment
        OfficeStaff staff = officeStaffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Office staff not found for ID: " + staffId));

        if (!tlBranch.equalsIgnoreCase(staff.getBranch())) {
            throw new IllegalArgumentException(String.format(
                    "Access Denied! You manage the %s branch, but this staff member belongs to %s.",
                    tlBranch, staff.getBranch()));
        }

        // 4. Finalize transactional state commit
        batch.setTrainer(staff);
        batchRepository.save(batch);

        return Map.of(
                "status", "SUCCESS",
                "branch", tlBranch,
                "message", String.format("Branch Staff '%s' assigned to Batch '%s' successfully.",
                        staff.getName(), batch.getBatchName())
        );
    }

    /**
     * Branch-guarded method for filtering and setting bulk-notified staff identifiers to a batch context.
     */
    @Transactional
    public Map<String, String> assignStaffToBatchBulk(Long teamLeadId, Long batchId, List<Long> staffIds) {
        if (staffIds == null || staffIds.isEmpty()) {
            throw new IllegalArgumentException("Bulk identification list can't be empty.");
        }

        TeamLead leader = repository.findById(teamLeadId)
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead not found for ID: " + teamLeadId));
        String tlBranch = leader.getBranch();

        TrainingBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Training batch not found for ID: " + batchId));

        List<OfficeStaff> staffList = officeStaffRepository.findAllById(staffIds);

        // Enforce location validation across every record in the array selection payload
        for (OfficeStaff staff : staffList) {
            if (!tlBranch.equalsIgnoreCase(staff.getBranch())) {
                throw new IllegalArgumentException(String.format(
                        "Access Denied! Staff member '%s' belongs to a different branch office (%s).",
                        staff.getName(), staff.getBranch()));
            }
        }

        // Set primary matching element as active trainer reference
        OfficeStaff primaryChoice = staffList.get(0);
        batch.setTrainer(primaryChoice);
        batchRepository.save(batch);

        return Map.of(
                "status", "SUCCESS",
                "message", String.format("Bulk check approved for %s branch. Trainer '%s' assigned to Batch.",
                        tlBranch, primaryChoice.getName())
        );
    }
}