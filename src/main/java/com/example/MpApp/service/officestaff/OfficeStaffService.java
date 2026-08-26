package com.example.MpApp.service.officestaff;

import com.example.MpApp.dto.file.FileViewResponse;
import com.example.MpApp.dto.officestaff.*;
import com.example.MpApp.entity.OtpEntity;
import com.example.MpApp.entity.officestaff.OfficeStaffLeave;
import com.example.MpApp.entity.officestaff.OfficeStaffPermission;
import com.example.MpApp.exception.InvalidCredentialsException;
import com.example.MpApp.exception.ResourceNotFoundException;
import com.example.MpApp.repository.OtpRepository;
import com.example.MpApp.repository.officestaff.OfficeStaffLeaveRepository;
import com.example.MpApp.repository.officestaff.OfficeStaffPermissionRepository;
import com.example.MpApp.repository.officestaff.OfficeStaffRepository;
import com.example.MpApp.repository.task.TaskRepository;
import com.example.MpApp.repository.task.TaskUpdateRepository;
import com.example.MpApp.config.JwtService;
import com.example.MpApp.dto.task.TaskResponse;
import com.example.MpApp.dto.task.TaskUpdateRequest;
import com.example.MpApp.entity.officestaff.OfficeStaff;
import com.example.MpApp.entity.task.Task;
import com.example.MpApp.entity.task.TaskUpdate;
import com.example.MpApp.entity.enums.TaskStatus;
import com.example.MpApp.service.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OfficeStaffService {

    private final OfficeStaffRepository repository;
    private final TaskRepository taskRepository;
    private final TaskUpdateRepository taskUpdateRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final OfficeStaffLeaveRepository leaveRepository;
    private final OfficeStaffPermissionRepository permissionRepository;
    private final OtpRepository otpRepository;

    private final OfficeStaffAttendanceService attendanceService;
    private final EmailService emailService;



    public String extractEmail(String authHeader){
        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            throw new RuntimeException("Token Required");
        }

        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);

        return email;
    }


    public Map<String, String> loginOfficeStaff(OfficeStaffLoginRequest request) {

        // Inside OfficeStaffService.java (Login method)
        OfficeStaff staff = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));

        if (!"APPROVED".equals(staff.getApprovalStatus())) {
            throw new IllegalStateException("Your account is pending Admin approval. Please wait.");
        }

// Proceed with token generation if approved...
        if (!passwordEncoder.matches(request.getPassword(), staff.getPassword())) {
            return Map.of("message", "Invalid Password");
        }

        UserDetails userDetails = User.builder()
                .username(staff.getEmail())
                .password(staff.getPassword())
                .roles("OFFICE_STAFF")
                .build();

        String token = jwtService.generateToken(userDetails);

        return Map.of(
                "staffId", staff.getId().toString(),
                "email", staff.getEmail(),
                "token", token
        );
    }

    // TASKS
    public List<TaskResponse> getMyTasks(String authHeader) {
        String email = extractEmail(authHeader);

        OfficeStaff staff = repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found for email: " + email));

        return taskRepository.findTasksByStaff(staff.getId());
    }

    public TaskResponse updateProgress(Long taskId,String authHeader , TaskUpdateRequest request) {

        String email = extractEmail(authHeader);

        OfficeStaff staff = repository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("Staff not found for email: " + email)
        );


        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        TaskUpdate update = new TaskUpdate();

        update.setTask(task);
        update.setProgressPercentage(request.getProgressPercentage());
        update.setWorkDoneToday(request.getWorkDoneToday());
        update.setBlockers(request.getBlockers());
        update.setComments(request.getComments());
        update.setUpdatedBy(staff);
        update.setAttachmentUrl(request.getAttachmentUrl());
        update.setStatus(request.getStatus());
        update.setUpdatedAt(LocalDateTime.now());

        taskUpdateRepository.save(update);

        task.setProgress(request.getProgressPercentage());
        task.setStatus(request.getStatus());

        Task tasks =  taskRepository.save(task);

        TaskResponse response = new TaskResponse();

        response.setTaskId(tasks.getId());
        response.setTitle(tasks.getTitle());
        response.setDescription(tasks.getDescription());
        response.setAssignedDate(tasks.getAssignedDate());
        response.setDeadline(tasks.getDeadline());
        response.setProgress(tasks.getProgress());
        response.setEstimatedHours(tasks.getEstimatedHours());
        response.setStatus(tasks.getStatus());
        response.setPriority(tasks.getPriority());
        response.setTaskType(tasks.getTaskType());
        response.setStaffId(tasks.getStaff().getId());
        response.setStaffName(tasks.getStaff().getName());
        response.setStaffRole(String.valueOf(tasks.getStaff().getCategory()));
        response.setStaffIdCode(tasks.getStaff().getStaffId());
        response.setTeamLeadId(tasks.getTeamLead().getId());
        response.setTeamLeadName(tasks.getTeamLead().getName());
        response.setTeamLeadIdCode(tasks.getTeamLead().getTeamLeadId());

        return response;




    }

    public TaskResponse submitTask(Long taskId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setStatus(TaskStatus.WAITING_FOR_REVIEW);

        Task tasks =  taskRepository.save(task);

        TaskResponse response = new TaskResponse();

        response.setTaskId(tasks.getId());
        response.setTitle(tasks.getTitle());
        response.setDescription(tasks.getDescription());
        response.setAssignedDate(tasks.getAssignedDate());
        response.setDeadline(tasks.getDeadline());
        response.setProgress(tasks.getProgress());
        response.setEstimatedHours(tasks.getEstimatedHours());
        response.setStatus(tasks.getStatus());
        response.setPriority(tasks.getPriority());
        response.setTaskType(tasks.getTaskType());
        response.setStaffId(tasks.getStaff().getId());
        response.setStaffName(tasks.getStaff().getName());
        response.setStaffRole(String.valueOf(tasks.getStaff().getCategory()));
        response.setStaffIdCode(tasks.getStaff().getStaffId());
        response.setTeamLeadId(tasks.getTeamLead().getId());
        response.setTeamLeadName(tasks.getTeamLead().getName());
        response.setTeamLeadIdCode(tasks.getTeamLead().getTeamLeadId());

        return response;
    }

    public OfficeStaffProfileResponse getProfile(Long staffId) {
        OfficeStaff staff = repository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));

        OfficeStaffProfileResponse response = new OfficeStaffProfileResponse();
        // ... existing assignments ...
        response.setScore(staff.getScore());

        // Fetch and set the new aggregated metrics
        PerformanceSummaryDTO metrics = this.getStaffPerformanceSummary(staffId);
        response.setPerformanceMetrics(metrics);

        return response;
    }

    public OfficeStaffLeave requestLeave(Long staffId, LeaveRequestDTO request) {
        OfficeStaff staff = repository.findById(staffId).orElseThrow();
        OfficeStaffLeave leave = new OfficeStaffLeave();
        leave.setStaff(staff);
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setReason(request.getReason());
        leave.setStatus("PENDING");
        return leaveRepository.save(leave);
    }

    public PerformanceSummaryDTO getStaffPerformanceSummary(Long staffId) {
        OfficeStaff staff = repository.findById(staffId).orElseThrow();

        long completed = taskRepository.countByStaffIdAndStatus(staffId, TaskStatus.COMPLETED);
        long pending = taskRepository.countByStaffIdAndStatus(staffId, TaskStatus.PENDING);

        // Calculate dynamic approval rate
        double approvalRate = taskRepository.calculateApprovalRate(staffId);

        // Use your attendance service to get history and calc percentage
        double attendancePercent = attendanceService.calculateAttendancePercentage(staffId);

        return new PerformanceSummaryDTO(staff.getName(), staff.getScore(), completed, pending, approvalRate, attendancePercent);
    }

    public List<OfficeStaff> getLeaderboard() {
        return repository.findAllByOrderByScoreDesc();
    }

    // In OfficeStaffService.java

    public List<OfficeStaffLeave> getLeaveHistory(Long staffId) {
        // 1. Verify staff exists
        repository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        // 2. Fetch history
        return leaveRepository.findByStaffIdWithStaff(staffId);
    }

    // Inject this field along with your other repositories at the top of your OfficeStaffService:
// private final OfficeStaffPermissionRepository permissionRepository;

// ================= PERMISSION MANAGEMENT (NEW FEATURES) =================

    /*
    ===================================
    REQUEST PERMISSION (BRANCH RESTRICTED)
    ===================================
    */
    @Transactional
    public Map<String, String> requestPermission(Long staffId, PermissionRequestDTO request) {
        OfficeStaff staff = repository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff index missing for ID: " + staffId));

        // 1. Validate duration bounds (Only 1-hour or 2-hour slots permitted)
        if (request.getDurationHours() != 1 && request.getDurationHours() != 2) {
            throw new IllegalArgumentException("Invalid permission duration. Only 1-hour or 2-hour slots are permitted.");
        }

        // 2. Identify the staff's branch location
        // NOTE: Adjust "getBranch()" to match your exact entity field name (e.g., getBranchName(), getBranchLocation())
        String branch = staff.getBranch() != null ? staff.getBranch().trim() : "";

        // 3. Conditional Rule: Apply strict monthly quota ONLY for Tirunelveli and Thisayanvilai branches
        if ("TIRUNELVELI".equals(branch) || "THISAYANVILAI".equals(branch)) {

            int targetMonth = request.getPermissionDate().getMonthValue();
            int targetYear = request.getPermissionDate().getYear();

            long permissionsCountThisMonth = permissionRepository.countPermissionsByStaffAndMonth(
                    staffId, targetMonth, targetYear
            );

            if (permissionsCountThisMonth >= 2) {
                throw new IllegalStateException("Monthly quota exceeded. Staff in " + branch +
                        " branch are strictly limited to a maximum of 2 permissions per calendar month.");
            }
        }

        // 4. Structural Mappings & Save
        OfficeStaffPermission permission = new OfficeStaffPermission();
        permission.setStaff(staff);
        permission.setPermissionDate(request.getPermissionDate());
        permission.setDurationHours(request.getDurationHours());
        permission.setReason(request.getReason());
        permission.setStatus("PENDING");

        OfficeStaffPermission savedPermission = permissionRepository.save(permission);

        // 5. Build tracking payload response
        Map<String, String> response = new HashMap<>();
        response.put("permissionId", savedPermission.getId().toString());
        response.put("branch", branch);
        response.put("status", "PENDING");
        response.put("message", "Permission Request Logged Successfully for " + branch + " branch.");
        return response;
    }

    // UNCHANGED GET UNTOUCHED METHOD FOR HISTORY AUDIT
    public List<OfficeStaffPermission> getPermissionHistory(Long staffId) {
        repository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff validation footprint missing for ID: " + staffId));

        return permissionRepository.findByStaffIdOrderByPermissionDateDesc(staffId);
    }

    // Inject your officeStaffRepository directly here

    // Add this map at the class level of OfficeStaffService
    private final Map<String, String> otpStorage = new HashMap<>();

    @Transactional
    public String sendOtp(String email) {
        OfficeStaff staff = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email Not Found"));

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

        // Clear any existing OTP
        otpRepository.deleteByEmail(email);

        // Store in DB
        OtpEntity otpEntity = new OtpEntity();
        otpEntity.setEmail(email);
        otpEntity.setOtpCode(otp);
        otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otpRepository.save(otpEntity);

        emailService.sendOtpEmail(email, otp);
        return "OTP sent successfully to your registered email.";
    }

    public String verifyOtp(String email, String otp) {
        OtpEntity otpEntity = otpRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("OTP not requested"));

        if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            otpRepository.deleteByEmail(email);
            throw new RuntimeException("OTP has expired");
        }

        if (!otpEntity.getOtpCode().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        return "OTP Verified Successfully";
    }

    @Transactional
    public String resetPassword(String email, String otp, String newPassword) {
        // Reuse verification logic
        verifyOtp(email, otp);

        OfficeStaff staff = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email Not Found"));

        staff.setPassword(passwordEncoder.encode(newPassword));
        repository.save(staff);

        // Consume OTP
        otpRepository.deleteByEmail(email);
        return "Password Reset Successful";
    }
    public FileViewResponse getStaffFiles(Long id) {
        OfficeStaff staff = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff Member Not Found"));
        return new FileViewResponse(staff.getProfilePhoto(), staff.getAadhaarFile(), staff.getResumeFile());
    }

    @Transactional
    public String changePassword(String email, String oldPassword, String newPassword) {
        OfficeStaff staff = repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found for email: " + email));

        // 1. Verify the old password is correct
        if (!passwordEncoder.matches(oldPassword, staff.getPassword())) {
            throw new InvalidCredentialsException("Invalid Old Password");
        }

        // 2. SECURITY CHECK: Prevent using the old password as the new one
        if (passwordEncoder.matches(newPassword, staff.getPassword())) {
            throw new IllegalStateException("New password cannot be the same as your old password.");
        }

        // 3. Encrypt and set the new password
        staff.setPassword(passwordEncoder.encode(newPassword));
        repository.save(staff);

        return "Password Changed Successfully";
    }
}