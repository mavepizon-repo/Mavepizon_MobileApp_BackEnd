package com.example.MpApp.service.officestaff;

import com.example.MpApp.security.PasswordPolicy;

import com.example.MpApp.dto.file.FileViewResponse;
import com.example.MpApp.dto.officestaff.*;
import com.example.MpApp.dto.task.TaskResponse;
import com.example.MpApp.dto.task.TaskUpdateRequest;
import com.example.MpApp.dto.task.TaskUpdateResponse;

import com.example.MpApp.entity.OtpEntity;
import com.example.MpApp.entity.admin.Admin;
import com.example.MpApp.entity.enums.TaskStatus;
import com.example.MpApp.entity.officestaff.OfficeStaff;
import com.example.MpApp.entity.officestaff.OfficeStaffLeave;
import com.example.MpApp.entity.officestaff.OfficeStaffPermission;
import com.example.MpApp.entity.task.Task;
import com.example.MpApp.entity.task.TaskUpdate;

import com.example.MpApp.exception.ForbiddenException;
import com.example.MpApp.exception.InvalidCredentialsException;
import com.example.MpApp.exception.ResourceNotFoundException;

import com.example.MpApp.repository.OtpRepository;
import com.example.MpApp.repository.admin.AdminRepository;
import com.example.MpApp.repository.officestaff.OfficeStaffLeaveRepository;
import com.example.MpApp.repository.officestaff.OfficeStaffPermissionRepository;
import com.example.MpApp.repository.officestaff.OfficeStaffRepository;
import com.example.MpApp.repository.task.TaskRepository;
import com.example.MpApp.repository.task.TaskUpdateRepository;

import com.example.MpApp.config.JwtService;
import com.example.MpApp.service.EmailService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
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


    // =========================================================
    // GET CURRENT ROLE
    // =========================================================

    public String getMyRole() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assert authentication != null;

        return authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("NO_ROLE");
    }


    // =========================================================
    // EXTRACT EMAIL FROM JWT
    // =========================================================

    public String extractEmail(String authHeader) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            throw new RuntimeException("Token Required");
        }

        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);

        return email;
    }


    // =========================================================
    // LOGIN
    // =========================================================

    public Map<String, String> loginOfficeStaff(
            OfficeStaffLoginRequest request) {

        OfficeStaff staff =
                repository.findByEmail(request.getEmail())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Staff not found"
                                )
                        );

        if (!"APPROVED".equals(staff.getApprovalStatus())) {

            throw new IllegalStateException(
                    "Your account is pending Admin approval. Please wait."
            );
        }

        if (!staff.isActive()) {
            throw new IllegalStateException("Your account is inactive");
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                staff.getPassword())) {

            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }

        UserDetails userDetails =
                User.builder()
                        .username(staff.getEmail())
                        .password(staff.getPassword())
                        .roles("OFFICE_STAFF")
                        .build();

        String token =
                jwtService.generateToken(userDetails);

        String role =
                staff.getRole() == null
                        ? ""
                        : staff.getRole();

        String category =
                staff.getCategory() == null
                        ? ""
                        : staff.getCategory().name();

        return Map.of(
                "staffId",
                staff.getId().toString(),

                "email",
                staff.getEmail(),

                "category",
                category,

                "token",
                token,

                "role",
                role
        );
    }


    // =========================================================
    // TASKS
    // =========================================================

    public List<TaskResponse> getMyTasks(
            String authHeader) {

        String email =
                extractEmail(authHeader);

        OfficeStaff staff =
                repository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Staff not found for email: "
                                                + email
                                )
                        );

        return taskRepository.findTasksByStaff(
                staff.getId()
        );
    }


    public TaskUpdateResponse updateProgress(
            Long taskId,
            String authHeader,
            TaskUpdateRequest request) {

        String email =
                extractEmail(authHeader);

        String role =
                getMyRole();

        OfficeStaff staff =
                repository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Staff not found for email: "
                                                + email
                                )
                        );

        Task task =
                taskRepository.findById(taskId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Task not found"
                                )
                        );

        TaskUpdate update =
                new TaskUpdate();

        update.setTask(task);
        update.setProgressPercentage(
                request.getProgressPercentage()
        );
        update.setWorkDoneToday(
                request.getWorkDoneToday()
        );
        update.setBlockers(
                request.getBlockers()
        );
        update.setComments(
                request.getComments()
        );
        update.setUpdatedBy(staff);
        update.setAttachmentUrl(
                request.getAttachmentUrl()
        );
        update.setStatus(
                request.getStatus()
        );
        update.setUpdatedAt(
                LocalDateTime.now()
        );

        taskUpdateRepository.save(update);

        task.setProgress(
                request.getProgressPercentage()
        );

        task.setStatus(
                request.getStatus()
        );

        Task tasks =
                taskRepository.save(task);

        TaskUpdateResponse response =
                new TaskUpdateResponse();

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
        response.setStaffId(
                tasks.getStaff().getId()
        );
        response.setStaffName(
                tasks.getStaff().getName()
        );
        response.setStaffRole(
                String.valueOf(
                        tasks.getStaff().getCategory()
                )
        );
        response.setStaffIdCode(
                tasks.getStaff().getStaffId()
        );

        if (tasks.getTeamLead() == null) {

            Admin admin =
                    tasks.getAdmin();

            response.setCreatedBy(
                    admin.getEmail()
            );

            response.setCreatorRole(
                    "ADMIN"
            );

        } else {

            response.setCreatedBy(
                    tasks.getTeamLead().getEmail()
            );

            response.setCreatorRole(
                    "TEAM_LEAD"
            );
        }

        return response;
    }


    public TaskResponse submitTask(
            Long taskId) {

        Task task =
                taskRepository.findById(taskId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Task not found"
                                )
                        );

        task.setStatus(
                TaskStatus.WAITING_FOR_REVIEW
        );

        Task tasks =
                taskRepository.save(task);

        TaskResponse response =
                new TaskResponse();

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
        response.setStaffId(
                tasks.getStaff().getId()
        );
        response.setStaffName(
                tasks.getStaff().getName()
        );
        response.setStaffRole(
                String.valueOf(
                        tasks.getStaff().getCategory()
                )
        );
        response.setStaffIdCode(
                tasks.getStaff().getStaffId()
        );
        response.setTeamLeadId(
                tasks.getTeamLead().getId()
        );
        response.setTeamLeadName(
                tasks.getTeamLead().getName()
        );
        response.setTeamLeadIdCode(
                tasks.getTeamLead().getTeamLeadId()
        );

        return response;
    }


    // =========================================================
    // PROFILE - OWNERSHIP PROTECTED
    // =========================================================

    public OfficeStaffProfileResponse getProfile(
            Long staffId,
            String authHeader) {

        String email =
                extractEmail(authHeader);

        OfficeStaff authenticatedStaff =
                repository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Authenticated staff not found"
                                )
                        );

        if (!authenticatedStaff.getId().equals(staffId)) {

            throw new ForbiddenException(
                    "You are not allowed to access another staff member's profile"
            );
        }

        OfficeStaff staff =
                authenticatedStaff;

        OfficeStaffProfileResponse response =
                new OfficeStaffProfileResponse();

        response.setId(staff.getId());
        response.setName(staff.getName());
        response.setEmail(staff.getEmail());
        response.setRole(
                staff.getRole() == null
                        ? ""
                        : staff.getRole()
        );
        response.setScore(
                staff.getScore()
        );

        PerformanceSummaryDTO metrics =
                this.getStaffPerformanceSummary(
                        staff.getId(),
                        authHeader
                );

        response.setPerformanceMetrics(
                metrics
        );

        return response;
    }


    // =========================================================
    // REQUEST LEAVE - OWNERSHIP PROTECTED
    // =========================================================

    public OfficeStaffLeave requestLeave(
            Long staffId,
            LeaveRequestDTO request,
            String authHeader) {

        String email =
                extractEmail(authHeader);

        OfficeStaff authenticatedStaff =
                repository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Authenticated staff not found"
                                )
                        );

        if (!authenticatedStaff.getId().equals(staffId)) {

            throw new ForbiddenException(
                    "You are not allowed to request leave for another staff member"
            );
        }

        OfficeStaff staff =
                authenticatedStaff;

        OfficeStaffLeave leave =
                new OfficeStaffLeave();

        leave.setStaff(staff);
        leave.setStartDate(
                request.getStartDate()
        );
        leave.setEndDate(
                request.getEndDate()
        );
        leave.setReason(
                request.getReason()
        );
        leave.setStatus(
                "PENDING"
        );

        return leaveRepository.save(
                leave
        );
    }


    // =========================================================
    // PERFORMANCE SUMMARY - OWNERSHIP PROTECTED
    // =========================================================

    public PerformanceSummaryDTO getStaffPerformanceSummary(
            Long staffId,
            String authHeader) {

        String email =
                extractEmail(authHeader);

        OfficeStaff authenticatedStaff =
                repository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Authenticated staff not found"
                                )
                        );

        if (!authenticatedStaff.getId().equals(staffId)) {

            throw new ForbiddenException(
                    "You are not allowed to access another staff member's performance summary"
            );
        }

        OfficeStaff staff =
                authenticatedStaff;

        long completed =
                taskRepository.countByStaffIdAndStatus(
                        staff.getId(),
                        TaskStatus.COMPLETED
                );

        long pending =
                taskRepository.countByStaffIdAndStatus(
                        staff.getId(),
                        TaskStatus.PENDING
                );

        double approvalRate =
                taskRepository.calculateApprovalRate(
                        staff.getId()
                );

        double attendancePercent =
                attendanceService.calculateAttendancePercentage(
                        staff.getId()
                );

        return new PerformanceSummaryDTO(
                staff.getName(),
                staff.getScore(),
                completed,
                pending,
                approvalRate,
                attendancePercent
        );
    }


    // =========================================================
    // LEADERBOARD
    // =========================================================

    public List<OfficeStaff> getLeaderboard() {

        return repository.findAllByOrderByScoreDesc();
    }


    // =========================================================
    // LEAVE HISTORY - OWNERSHIP PROTECTED
    // =========================================================

    public List<OfficeStaffLeave> getLeaveHistory(
            Long staffId,
            String authHeader) {

        String email =
                extractEmail(authHeader);

        OfficeStaff authenticatedStaff =
                repository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Authenticated staff not found"
                                )
                        );

        if (!authenticatedStaff.getId().equals(staffId)) {

            throw new ForbiddenException(
                    "You are not allowed to access another staff member's leave history"
            );
        }

        return leaveRepository.findByStaffIdWithStaff(
                authenticatedStaff.getId()
        );
    }


    // =========================================================
    // REQUEST PERMISSION
    // =========================================================

    @Transactional
    public Map<String, String> requestPermission(
            String authHeader,
            PermissionRequestDTO request) {

        String email =
                extractEmail(authHeader);

        OfficeStaff staff =
                repository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Staff index missing for email : "
                                                + email
                                )
                        );

        if (request.getDurationHours() != 1 &&
                request.getDurationHours() != 2) {

            throw new IllegalArgumentException(
                    "Invalid permission duration. Only 1-hour or 2-hour slots are permitted."
            );
        }

        String branch =
                staff.getBranch() != null
                        ? staff.getBranch().trim()
                        : "";

        if ("TIRUNELVELI".equals(branch) ||
                "THISAYANVILAI".equals(branch)) {

            int targetMonth =
                    request.getPermissionDate()
                            .getMonthValue();

            int targetYear =
                    request.getPermissionDate()
                            .getYear();

            long permissionsCountThisMonth =
                    permissionRepository
                            .countPermissionsByStaffAndMonth(
                                    staff.getId(),
                                    targetMonth,
                                    targetYear
                            );

            if (permissionsCountThisMonth >= 2) {

                throw new IllegalStateException(
                        "Monthly quota exceeded. Staff in "
                                + branch
                                + " branch are strictly limited to a maximum of 2 permissions per calendar month."
                );
            }
        }

        OfficeStaffPermission permission =
                new OfficeStaffPermission();

        permission.setStaff(staff);
        permission.setPermissionDate(
                request.getPermissionDate()
        );
        permission.setDurationHours(
                request.getDurationHours()
        );
        permission.setReason(
                request.getReason()
        );
        permission.setStatus(
                "PENDING"
        );

        OfficeStaffPermission savedPermission =
                permissionRepository.save(
                        permission
                );

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "permissionId",
                savedPermission.getId().toString()
        );

        response.put(
                "branch",
                branch
        );

        response.put(
                "status",
                "PENDING"
        );

        response.put(
                "message",
                "Permission Request Logged Successfully for "
                        + branch
                        + " branch."
        );

        return response;
    }


    // =========================================================
    // PERMISSION HISTORY
    // =========================================================

    public List<PermissionResponseDTO>
    getPermissionHistory(
            String authHeader) {

        String email =
                extractEmail(authHeader);

        OfficeStaff staff =
                repository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Staff validation footprint missing for Email: "
                                                + email
                                )
                        );

        List<OfficeStaffPermission> permissions =
                permissionRepository
                        .findByStaffIdOrderByPermissionDateDesc(
                                staff.getId()
                        );

        return permissions.stream()
                .map(permission -> {

                    PermissionResponseDTO dto =
                            new PermissionResponseDTO();

                    dto.setId(
                            permission.getId()
                    );

                    dto.setPermissionDate(
                            permission.getPermissionDate()
                    );

                    dto.setDurationHours(
                            permission.getDurationHours()
                    );

                    dto.setReason(
                            permission.getReason()
                    );

                    dto.setStatus(
                            permission.getStatus()
                    );

                    dto.setCreatedAt(
                            permission.getCreatedAt()
                    );

                    dto.setStaffName(
                            permission.getStaff().getName()
                    );

                    dto.setBranch(
                            permission.getStaff().getBranch()
                    );

                    return dto;
                })
                .toList();
    }


    // =========================================================
    // OTP
    // =========================================================

    private static final SecureRandom OTP_RANDOM =
            new SecureRandom();


    @Transactional
    public String sendOtp(
            String email) {

        repository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Email Not Found"
                        )
                );

        OtpEntity existing =
                otpRepository
                        .findByEmail(email)
                        .orElse(null);

        if (existing != null &&
                existing.getLastSentAt() != null &&
                existing.getLastSentAt()
                        .plusSeconds(30)
                        .isAfter(LocalDateTime.now())) {

            throw new RuntimeException(
                    "Please wait before requesting another OTP"
            );
        }

        String otp =
                String.format(
                        "%06d",
                        OTP_RANDOM.nextInt(1_000_000)
                );

        otpRepository.deleteByEmail(email);

        OtpEntity otpEntity =
                new OtpEntity();

        otpEntity.setEmail(email);

        otpEntity.setOtpCode(
                passwordEncoder.encode(otp)
        );

        otpEntity.setExpiryTime(
                LocalDateTime.now().plusMinutes(5)
        );

        otpEntity.setVerificationAttempts(
                0
        );

        otpEntity.setLastSentAt(
                LocalDateTime.now()
        );

        otpRepository.save(
                otpEntity
        );

        emailService.sendOtpEmail(
                email,
                otp
        );

        return "OTP sent successfully to your registered email.";
    }


    @Transactional
    public String verifyOtp(
            String email,
            String otp) {

        OtpEntity otpEntity =
                otpRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "OTP not requested"
                                )
                        );

        if (otpEntity.getExpiryTime()
                .isBefore(LocalDateTime.now())) {

            otpRepository.deleteByEmail(email);

            throw new RuntimeException(
                    "OTP has expired"
            );
        }

        if (otpEntity.getVerificationAttempts() >= 5) {

            otpRepository.deleteByEmail(email);

            throw new RuntimeException(
                    "Too many failed OTP attempts"
            );
        }

        if (!passwordEncoder.matches(
                otp,
                otpEntity.getOtpCode())) {

            otpEntity.setVerificationAttempts(
                    otpEntity.getVerificationAttempts() + 1
            );

            if (otpEntity.getVerificationAttempts() >= 5) {

                otpRepository.deleteByEmail(
                        email
                );

            } else {

                otpRepository.save(
                        otpEntity
                );
            }

            throw new RuntimeException(
                    "Invalid OTP"
            );
        }

        otpRepository.deleteByEmail(
                email
        );

        return "OTP Verified Successfully";
    }


    @Transactional
    public String resetPassword(
            String email,
            String otp,
            String newPassword) {

        verifyOtp(
                email,
                otp
        );

        OfficeStaff staff =
                repository.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Email Not Found"
                                )
                        );

        PasswordPolicy.validate(newPassword);
        staff.setPassword(
                passwordEncoder.encode(
                        newPassword
                )
        );

        repository.save(
                staff
        );

        otpRepository.deleteByEmail(
                email
        );

        return "Password Reset Successful";
    }


    // =========================================================
    // STAFF FILES - OWNERSHIP PROTECTED
    // =========================================================

    public FileViewResponse getStaffFiles(
            Long id,
            String authHeader) {

        String email =
                extractEmail(authHeader);

        OfficeStaff authenticatedStaff =
                repository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Authenticated staff not found"
                                )
                        );

        if (!authenticatedStaff.getId().equals(id)) {

            throw new ForbiddenException(
                    "You are not allowed to access another staff member's files"
            );
        }

        OfficeStaff staff =
                authenticatedStaff;

        return new FileViewResponse(
                staff.getProfilePhoto(),
                staff.getAadhaarFile(),
                staff.getResumeFile()
        );
    }


    // =========================================================
    // CHANGE PASSWORD
    // =========================================================

    @Transactional
    public String changePassword(
            String email,
            String oldPassword,
            String newPassword) {

        OfficeStaff staff =
                repository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Staff not found for email: "
                                                + email
                                )
                        );

        if (!passwordEncoder.matches(
                oldPassword,
                staff.getPassword())) {

            throw new InvalidCredentialsException(
                    "Invalid Old Password"
            );
        }

        if (passwordEncoder.matches(
                newPassword,
                staff.getPassword())) {

            throw new IllegalStateException(
                    "New password cannot be the same as your old password."
            );
        }

        PasswordPolicy.validate(newPassword);
        staff.setPassword(
                passwordEncoder.encode(
                        newPassword
                )
        );

        repository.save(
                staff
        );

        return "Password Changed Successfully";
    }
}
