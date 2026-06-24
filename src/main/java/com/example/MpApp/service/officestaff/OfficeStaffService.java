package com.example.MpApp.service.officestaff;

import com.example.MpApp.dto.file.FileViewResponse;
import com.example.MpApp.dto.officestaff.LeaveRequestDTO;
import com.example.MpApp.dto.officestaff.OfficeStaffProfileResponse;
import com.example.MpApp.dto.officestaff.PermissionRequestDTO;
import com.example.MpApp.entity.officestaff.OfficeStaffLeave;
import com.example.MpApp.entity.officestaff.OfficeStaffPermission;
import com.example.MpApp.exception.ResourceNotFoundException;
import com.example.MpApp.repository.officestaff.OfficeStaffLeaveRepository;
import com.example.MpApp.repository.officestaff.OfficeStaffPermissionRepository;
import com.example.MpApp.repository.officestaff.OfficeStaffRepository;
import com.example.MpApp.repository.task.TaskRepository;
import com.example.MpApp.repository.task.TaskUpdateRepository;
import com.example.MpApp.config.JwtService;
import com.example.MpApp.dto.officestaff.OfficeStaffLoginRequest;
import com.example.MpApp.dto.task.TaskResponse;
import com.example.MpApp.dto.task.TaskUpdateRequest;
import com.example.MpApp.entity.officestaff.OfficeStaff;
import com.example.MpApp.entity.task.Task;
import com.example.MpApp.entity.task.TaskUpdate;
import com.example.MpApp.entity.enums.TaskStatus;
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

    // LOGIN


    public Map<String, String> loginOfficeStaff(OfficeStaffLoginRequest request) {

        OfficeStaff staff = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Not found"));

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
    public List<TaskResponse> getMyTasks(Long staffId) {
        return taskRepository.findTasksByStaff(staffId);
    }

    public Task updateProgress(Long taskId, TaskUpdateRequest request) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

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

        return taskRepository.save(task);
    }

    public Task submitTask(Long taskId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setStatus(TaskStatus.WAITING_FOR_REVIEW);

        return taskRepository.save(task);
    }

    public OfficeStaffProfileResponse getProfile(
            Long staffId)
    {
        OfficeStaff staff =
                repository.findById(staffId)
                        .orElseThrow();

        OfficeStaffProfileResponse response =
                new OfficeStaffProfileResponse();

        response.setId(staff.getId());
        response.setName(staff.getName());
        response.setEmail(staff.getEmail());
        response.setRole(staff.getRole());
        response.setScore(staff.getScore());

        response.setAssignedTasks(
                taskRepository.countByStaffId(
                        staffId));

        response.setCompletedTasks(
                taskRepository
                        .countByStaffIdAndStatus(
                                staffId,
                                TaskStatus.COMPLETED));

        response.setPendingTasks(
                taskRepository
                        .countByStaffIdAndStatus(
                                staffId,
                                TaskStatus.PENDING));

        response.setRejectedTasks(
                taskRepository
                        .countByStaffIdAndStatus(
                                staffId,
                                TaskStatus.REWORK_REQUIRED));

        response.setProfile(staff.getProfilePhoto());

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

    public String sendOtp(String email) {
        OfficeStaff staff = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email Not Found"));

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        otpStorage.put(email, otp);
        System.out.println("OTP for Office Staff (" + email + ") is: " + otp);
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

        OfficeStaff staff = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email Not Found"));

        staff.setPassword(passwordEncoder.encode(newPassword));
        repository.save(staff);
        otpStorage.remove(email);
        return "Password Reset Successful";
    }

    public FileViewResponse getStaffFiles(Long id) {
        OfficeStaff staff = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff Member Not Found"));
        return new FileViewResponse(staff.getProfilePhoto(), staff.getAadhaarFile(), staff.getResumeFile());
    }
}