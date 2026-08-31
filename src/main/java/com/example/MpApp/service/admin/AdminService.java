package com.example.MpApp.service.admin;

import com.example.MpApp.config.JwtService;
import com.example.MpApp.dto.admin.AdminLoginRequest;
import com.example.MpApp.dto.officestaff.StaffResponseDTO;
import com.example.MpApp.dto.task.*;
import com.example.MpApp.dto.teamlead.TeamLeadPerformanceDTO;
import com.example.MpApp.dto.teamlead.TeamLeadPermissionRequestDTO;
import com.example.MpApp.dto.teamlead.TeamLeadPermissionResponseDTO;
import com.example.MpApp.entity.OtpEntity;
import com.example.MpApp.entity.admin.Admin;
import com.example.MpApp.entity.collegestaff.CollegeStaff;
import com.example.MpApp.entity.enums.StaffCategory;
import com.example.MpApp.entity.enums.TaskStatus;
import com.example.MpApp.entity.enums.VerificationStatus;
import com.example.MpApp.entity.officestaff.OfficeStaff;
import com.example.MpApp.entity.student.Student;
import com.example.MpApp.entity.task.Task;
import com.example.MpApp.entity.task.TaskReview;
import com.example.MpApp.entity.teamlead.TeamLead;
import com.example.MpApp.entity.teamlead.TeamLeadLeave;
import com.example.MpApp.entity.teamlead.TeamLeadPermission;
import com.example.MpApp.exception.DuplicateResourceException;
import com.example.MpApp.exception.InvalidCredentialsException;
import com.example.MpApp.exception.ResourceNotFoundException;
import com.example.MpApp.repository.OtpRepository;
import com.example.MpApp.repository.admin.AdminRepository;
import com.example.MpApp.repository.collegestaff.CollegeStaffRepository;
import com.example.MpApp.repository.course.CourseRepository;
import com.example.MpApp.repository.officestaff.OfficeStaffRepository;
import com.example.MpApp.repository.student.StudentRepository;
import com.example.MpApp.repository.task.TaskRepository;
import com.example.MpApp.repository.task.TaskReviewRepository;
import com.example.MpApp.repository.task.TaskUpdateRepository;
import com.example.MpApp.repository.teamlead.TeamLeadLeaveRepository;
import com.example.MpApp.repository.teamlead.TeamLeadPermissionRepository;
import com.example.MpApp.repository.teamlead.TeamLeadRepository;
import com.example.MpApp.service.CloudinaryService;
import com.example.MpApp.service.EmailService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final CourseRepository courseRepository;
    private final OtpRepository otpRepository;

    private final CloudinaryService cloudinaryService;
    private final EmailService emailService;


    // =========================================================
    // GENERATE TEAM LEAD ID
    // =========================================================

    private String generateTeamLeadId(String branch) {

        String branchCode = switch (branch.trim().toUpperCase()) {
            case "TIRUNELVELI" -> "TVL";
            case "THISAYANVILAI" -> "TSV";
            case "NAGERCOIL" -> "NGL";
            case "COIMBATORE" -> "CBT";
            default -> throw new IllegalArgumentException(
                    "Invalid Branch: " + branch
            );
        };

        String prefix = "MP" + branchCode + "TL";

        String maxId =
                teamLeadRepository.findMaxTeamLeadIdByPrefix(prefix);

        int nextSequence = 1;

        if (maxId != null) {
            try {
                String sequencePart =
                        maxId.substring(prefix.length());

                nextSequence =
                        Integer.parseInt(sequencePart) + 1;

            } catch (Exception e) {
                nextSequence =
                        (int) teamLeadRepository.count() + 1;
            }
        }

        return prefix + String.format("%03d", nextSequence);
    }


    // =========================================================
    // GENERATE STAFF ID
    // =========================================================

    private String generateStaffId(
            String branch,
            StaffCategory category) {

        String branchCode = switch (branch.toUpperCase()) {
            case "COIMBATORE" -> "CBT";
            case "TIRUNELVELI" -> "TVL";
            case "THISAYANVILAI" -> "TSV";
            case "NAGERCOIL" -> "NGL";
            default -> throw new IllegalArgumentException(
                    "Invalid Branch: " + branch
            );
        };

        String categoryCode = switch (category) {
            case DEVELOPER -> "DE";
            case DEVELOPER_TRAINER -> "DT";
            case TELECOM_SERVICE -> "TE";
            case DESIGNER -> "DS";
            case FREELANCER -> "FL";
            default -> throw new IllegalArgumentException(
                    "Invalid Category: " + category
            );
        };

        long count = officeStaffRepository.count();

        return "MP"
                + branchCode
                + categoryCode
                + String.format("%03d", count + 1);
    }


    // =========================================================
    // ADMIN AUTH
    // =========================================================

    @Transactional
    public Map<String, String> registerAdmin(Admin admin) {

        if (adminRepository.findByEmail(admin.getEmail()).isPresent()) {

            throw new DuplicateResourceException(
                    "Admin already exists with email: "
                            + admin.getEmail()
            );
        }

        admin.setPassword(
                encoder.encode(admin.getPassword())
        );

        Admin savedAdmin =
                adminRepository.save(admin);

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "adminId",
                savedAdmin.getId().toString()
        );

        response.put(
                "message",
                "Admin registered successfully"
        );

        return response;
    }


    public Admin updateAdminFiles(
            Long id,
            MultipartFile profile) {

        Admin admin =
                adminRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Admin context not found for ID: " + id
                                )
                        );

        if (profile != null && !profile.isEmpty()) {

            admin.setProfile(
                    cloudinaryService.uploadFile(
                            profile,
                            "admin/profile"
                    )
            );
        }

        return adminRepository.save(admin);
    }


    public Map<String, String> login(
            AdminLoginRequest request) {

        Admin admin =
                adminRepository.findByEmail(
                                request.getEmail()
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Admin not found with email: "
                                                + request.getEmail()
                                )
                        );

        if (!encoder.matches(
                request.getPassword(),
                admin.getPassword()
        )) {

            throw new InvalidCredentialsException(
                    "Invalid password"
            );
        }

        UserDetails userDetails =
                User.builder()
                        .username(admin.getEmail())
                        .password(admin.getPassword())
                        .roles("ADMIN")
                        .build();

        String token =
                jwtService.generateToken(userDetails);

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "adminId",
                admin.getId().toString()
        );

        response.put(
                "userName",
                admin.getUserName()
        );

        response.put(
                "email",
                admin.getEmail()
        );

        response.put(
                "profile",
                admin.getProfile()
        );

        response.put(
                "token",
                token
        );

        response.put(
                "message",
                "Login successful"
        );

        return response;
    }


    // =========================================================
    // JWT VALIDATION
    // =========================================================

    public Admin validateAdminToken(
            String authHeader) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            throw new InvalidCredentialsException(
                    "Missing or invalid token layout"
            );
        }

        String token =
                authHeader.substring(7);

        String email =
                jwtService.extractEmail(token);

        return adminRepository.findByEmail(email)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Admin not found or token signature invalid"
                        )
                );
    }


    // =========================================================
    // TEAM LEAD MANAGEMENT
    // =========================================================

    @Transactional
    public Map<String, String> createTeamLead(
            Long adminId,
            TeamLead teamLead) {

        Admin admin =
                adminRepository.findById(adminId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Admin registration context missing. ID: "
                                                + adminId
                                )
                        );

        if (teamLeadRepository
                .findByEmail(teamLead.getEmail())
                .isPresent()) {

            throw new DuplicateResourceException(
                    "Team Lead already exists with email: "
                            + teamLead.getEmail()
            );
        }

        teamLead.setPassword(
                encoder.encode(teamLead.getPassword())
        );

        teamLead.setTeamLeadId(
                generateTeamLeadId(
                        teamLead.getBranch()
                )
        );

        teamLead.setCreatedByAdmin(admin);
        teamLead.setActive(true);

        TeamLead savedLead =
                teamLeadRepository.save(teamLead);

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "teamLeadId",
                savedLead.getId().toString()
        );

        response.put(
                "systemId",
                savedLead.getTeamLeadId()
        );

        response.put(
                "message",
                "Team Lead Created Successfully"
        );

        return response;
    }


    public TeamLead updateTeamLeadFiles(
            Long id,
            MultipartFile profile,
            MultipartFile aadhaar,
            MultipartFile resume) {

        TeamLead teamLead =
                teamLeadRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Team Lead context not found for ID: "
                                                + id
                                )
                        );

        if (profile != null && !profile.isEmpty()) {

            teamLead.setProfilePhoto(
                    cloudinaryService.uploadFile(
                            profile,
                            "teamLead/profile"
                    )
            );
        }

        if (aadhaar != null && !aadhaar.isEmpty()) {

            teamLead.setAadhaarFile(
                    cloudinaryService.uploadFile(
                            aadhaar,
                            "teamLead/aadhaar"
                    )
            );
        }

        if (resume != null && !resume.isEmpty()) {

            teamLead.setResumeFile(
                    cloudinaryService.uploadFile(
                            resume,
                            "teamLead/resume"
                    )
            );
        }

        return teamLeadRepository.save(teamLead);
    }


    public List<TeamLead> getAllTeamLeads() {

        return teamLeadRepository.findAll();
    }


    public TeamLead getTeamLeadById(Long id) {

        return teamLeadRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "TeamLead not found for ID: " + id
                        )
                );
    }


    public TeamLeadPerformanceDTO getTeamLeadPerformance(
            Long teamLeadId) {

        TeamLead tl =
                teamLeadRepository.findById(teamLeadId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Team Lead not found"
                                )
                        );

        long assigned =
                taskRepository.countByTeamLeadId(teamLeadId);

        long completed =
                taskRepository.countByTeamLeadIdAndStatus(
                        teamLeadId,
                        TaskStatus.COMPLETED
                );

        long pending =
                taskRepository.countByTeamLeadIdAndStatusIn(
                        teamLeadId,
                        List.of(
                                TaskStatus.ASSIGNED,
                                TaskStatus.PENDING,
                                TaskStatus.REWORK_REQUIRED
                        )
                );

        return new TeamLeadPerformanceDTO(
                tl.getId(),
                tl.getName(),
                tl.getPerformanceScore() != null
                        ? tl.getPerformanceScore()
                        : 0,
                assigned,
                completed,
                pending
        );
    }


    @Transactional
    public Map<String, String> updateTeamLead(
            Long id,
            TeamLead request) {

        TeamLead existing =
                teamLeadRepository.findById(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "TeamLead targeted for changes not found for ID: "
                                                + id
                                )
                        );

        if (request.getName() != null)
            existing.setName(request.getName());

        if (request.getRole() != null)
            existing.setRole(request.getRole());

        if (request.getMobileNumber() != null)
            existing.setMobileNumber(
                    request.getMobileNumber()
            );

        if (request.getExperience() != null)
            existing.setExperience(
                    request.getExperience()
            );

        if (request.getSkills() != null)
            existing.setSkills(request.getSkills());

        if (request.getActive() != null)
            existing.setActive(request.getActive());

        teamLeadRepository.save(existing);

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "teamLeadId",
                id.toString()
        );

        response.put(
                "message",
                "Team Lead Updated Successfully"
        );

        return response;
    }


    @Transactional
    public Map<String, String> toggleTeamLeadStatus(
            Long id,
            boolean active) {

        TeamLead lead =
                teamLeadRepository.findById(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "TeamLead record context missing for ID: "
                                                + id
                                )
                        );

        lead.setActive(active);

        teamLeadRepository.save(lead);

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "teamLeadId",
                id.toString()
        );

        response.put(
                "active",
                String.valueOf(active)
        );

        response.put(
                "message",
                "Team Lead Status Flag Toggled Successfully"
        );

        return response;
    }


    public void deleteTeamLead(Long id) {

        if (!teamLeadRepository.existsById(id)) {

            throw new ResourceNotFoundException(
                    "Cannot delete. TeamLead target configuration missing for ID: "
                            + id
            );
        }

        teamLeadRepository.deleteById(id);
    }


    // =========================================================
    // OFFICE STAFF MANAGEMENT
    // =========================================================

    @Transactional
    public Map<String, String> createStaff(
            Long adminId,
            OfficeStaff staff) {

        adminRepository.findById(adminId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Admin validation context tracking missing for ID: "
                                        + adminId
                        )
                );

        if (officeStaffRepository
                .findByEmail(staff.getEmail())
                .isPresent()) {

            throw new DuplicateResourceException(
                    "Office staff layout with email context already exists: "
                            + staff.getEmail()
            );
        }

        staff.setStaffId(
                generateStaffId(
                        staff.getBranch(),
                        staff.getCategory()
                )
        );

        staff.setPassword(
                encoder.encode(staff.getPassword())
        );

        staff.setScore(100);

        OfficeStaff savedStaff =
                officeStaffRepository.save(staff);

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "staffId",
                savedStaff.getId().toString()
        );

        response.put(
                "systemId",
                savedStaff.getStaffId()
        );

        response.put(
                "message",
                "Office Staff Created Successfully"
        );

        return response;
    }


    public Map<String ,String> setApprovalStatus(Long staffId , String status){
        OfficeStaff staff = officeStaffRepository.findById(staffId).orElseThrow(()->new ResourceNotFoundException("Staff not found for ID: "+staffId));
        staff.setApprovalStatus(status);
        officeStaffRepository.save(staff);
        Map<String ,String> response = new HashMap<>();
        response.put("staffId",staffId.toString());
        response.put("message","Staff Status Updated Successfully");
        return response;
    }


    public OfficeStaff updateStaffFiles(
            Long id,
            MultipartFile profile,
            MultipartFile aadhaar,
            MultipartFile resume) {

        OfficeStaff staff =
                officeStaffRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Office Staff context not found for ID: "
                                                + id
                                )
                        );

        if (profile != null && !profile.isEmpty()) {

            staff.setProfilePhoto(
                    cloudinaryService.uploadFile(
                            profile,
                            "officeStaff/profile"
                    )
            );
        }

        if (aadhaar != null && !aadhaar.isEmpty()) {

            staff.setAadhaarFile(
                    cloudinaryService.uploadFile(
                            aadhaar,
                            "officeStaff/aadhaar"
                    )
            );
        }

        if (resume != null && !resume.isEmpty()) {

            staff.setResumeFile(
                    cloudinaryService.uploadFile(
                            resume,
                            "officeStaff/resume"
                    )
            );
        }

        return officeStaffRepository.save(staff);
    }


    @Transactional
    public Map<String, String> updateStaff(
            Long id,
            OfficeStaff request) {

        OfficeStaff existing =
                officeStaffRepository.findById(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Staff workplace footprint not found for ID: "
                                                + id
                                )
                        );

        if (request.getName() != null)
            existing.setName(request.getName());

        if (request.getRole() != null)
            existing.setRole(request.getRole());

        if (request.getMobileNumber() != null)
            existing.setMobileNumber(
                    request.getMobileNumber()
            );

        if (request.getSkills() != null)
            existing.setSkills(request.getSkills());

        if (request.getBloodGroup() != null)
            existing.setBloodGroup(
                    request.getBloodGroup()
            );

        if (request.getEmployeeId() != null)
            existing.setEmployeeId(
                    request.getEmployeeId()
            );

        officeStaffRepository.save(existing);

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "staffId",
                id.toString()
        );

        response.put(
                "message",
                "Office Staff Updated Successfully"
        );

        return response;
    }


    public List<OfficeStaff> getAllStaff() {

        return officeStaffRepository.findAll();
    }


    public OfficeStaff getStaffById(Long id) {

        return officeStaffRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Staff footprint not found for ID: "
                                        + id
                        )
                );
    }


    public void deleteStaff(Long id) {

        if (!officeStaffRepository.existsById(id)) {

            throw new ResourceNotFoundException(
                    "Cannot delete. Office Staff instance missing for ID: "
                            + id
            );
        }

        officeStaffRepository.deleteById(id);
    }


    // =========================================================
    // TASK ASSIGNMENT
    // =========================================================

    @Transactional
    public Map<String, String> assignTaskByAdmin(
            Long adminId,
            TaskRequest request) {

        adminRepository.findById(adminId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Admin not found for ID: " + adminId
                        )
                );

        Task task = new Task();

        if (request.getStaffId() != null) {

            OfficeStaff staff =
                    officeStaffRepository.findById(
                                    request.getStaffId()
                            )
                            .orElseThrow(
                                    () -> new ResourceNotFoundException(
                                            "Staff not found"
                                    )
                            );

            task.setStaff(staff);
            task.setTeamLead(null);

        } else if (request.getTeamLeadId() != null) {

            TeamLead lead =
                    teamLeadRepository.findById(
                                    request.getTeamLeadId()
                            )
                            .orElseThrow(
                                    () -> new ResourceNotFoundException(
                                            "Team Lead not found"
                                    )
                            );

            task.setTeamLead(lead);
            task.setStaff(null);

        } else {

            throw new IllegalArgumentException(
                    "Must provide either Staff ID or Team Lead ID."
            );
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setAssignedDate(
                java.time.LocalDate.now()
        );
        task.setDeadline(request.getDeadline());
        task.setTaskType(request.getTaskType());
        task.setPriority(request.getPriority());
        task.setEstimatedHours(
                request.getEstimatedHours()
        );
        task.setRemarks(request.getRemarks());
        task.setStatus(TaskStatus.ASSIGNED);
        task.setProgress(0);

        Task savedTask =
                taskRepository.save(task);

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "taskId",
                savedTask.getId().toString()
        );

        response.put(
                "message",
                "Task Assigned Successfully by Admin"
        );

        return response;
    }


    // =========================================================
    // TASK MANAGEMENT
    // =========================================================

    public List<TaskResponse> getAllTasks() {

        return taskRepository.findAllTasks();
    }


    public TaskResponse getTaskById(Long taskId) {

        return taskRepository.findTaskById(taskId);
    }


    public List<TaskResponse> getTasksByStaff(
            Long staffId) {

        return taskRepository.findTasksByStaff(
                staffId
        );
    }


    public List<TaskResponse> getTasksByStatus(
            TaskStatus status) {

        return taskRepository.findTasksByStatus(
                status
        );
    }


    @Transactional
    public void deleteTaskByAdmin(Long taskId) {

        if (!taskRepository.existsById(taskId)) {

            throw new ResourceNotFoundException(
                    "Task not found for ID: " + taskId
            );
        }

        taskRepository.deleteById(taskId);
    }


    @Transactional
    public Map<String, String> updateTaskAdmin(
            Long taskId,
            TaskAdminUpdateRequest request) {

        Task task =
                taskRepository.findById(taskId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Target task context not found for ID: "
                                                + taskId
                                )
                        );

        if (request.getTitle() != null)
            task.setTitle(request.getTitle());

        if (request.getDescription() != null)
            task.setDescription(
                    request.getDescription()
            );

        if (request.getPriority() != null)
            task.setPriority(
                    request.getPriority()
            );

        if (request.getDeadline() != null)
            task.setDeadline(
                    request.getDeadline()
            );

        if (request.getStatus() != null)
            task.setStatus(
                    request.getStatus()
            );

        if (request.getEstimatedHours() != null)
            task.setEstimatedHours(
                    request.getEstimatedHours()
            );

        if (request.getRemarks() != null)
            task.setRemarks(
                    request.getRemarks()
            );

        if (request.getCompletionRemarks() != null)
            task.setCompletionRemarks(
                    request.getCompletionRemarks()
            );

        if (request.getTaskType() != null)
            task.setTaskType(
                    request.getTaskType()
            );

        taskRepository.save(task);

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "message",
                "All structural task metadata fields updated successfully."
        );

        return response;
    }


    @Transactional
    public Map<String, String> reviewTaskByAdmin(
            Long taskId,
            Long adminId,
            TaskReviewRequest request) {

        Task task =
                taskRepository.findById(taskId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Task not found for ID: "
                                                + taskId
                                )
                        );

        TaskReview review =
                new TaskReview();

        review.setTask(task);
        review.setVerificationStatus(
                request.getVerificationStatus()
        );
        review.setReviewComment(
                request.getReviewComment()
        );
        review.setReviewedAt(
                java.time.LocalDateTime.now()
        );

        task.setStatus(
                request.getVerificationStatus()
                        == VerificationStatus.APPROVED
                        ? TaskStatus.COMPLETED
                        : TaskStatus.REWORK_REQUIRED
        );

        taskRepository.save(task);
        taskReviewRepository.save(review);

        return Map.of(
                "message",
                "Task Reviewed by Admin successfully"
        );
    }


    // =========================================================
    // COLLEGE STAFF MANAGEMENT
    // =========================================================

    @Transactional
    public Map<String, String> createCollegeStaff(
            CollegeStaff staff) {

        if (collegeStaffRepository
                .existsByEmail(staff.getEmail())) {

            throw new DuplicateResourceException(
                    "College staff email footprint already registered: "
                            + staff.getEmail()
            );
        }

        if (collegeStaffRepository
                .existsByMobileNumber(
                        staff.getMobileNumber()
                )) {

            throw new DuplicateResourceException(
                    "College staff mobile layout sequence already registered: "
                            + staff.getMobileNumber()
            );
        }

        staff.setPassword(
                encoder.encode(staff.getPassword())
        );

        CollegeStaff savedStaff =
                collegeStaffRepository.save(staff);

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "collegeStaffId",
                savedStaff.getId().toString()
        );

        response.put(
                "message",
                "College Staff Created Successfully"
        );

        return response;
    }


    @Transactional
    public Map<String, String> updateCollegeStaff(
            Long id,
            CollegeStaff request) {

        CollegeStaff existing =
                collegeStaffRepository.findById(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "College Staff target mapping not found for ID: "
                                                + id
                                )
                        );

        if (request.getName() != null)
            existing.setName(request.getName());

        if (request.getCollegeName() != null)
            existing.setCollegeName(
                    request.getCollegeName()
            );

        if (request.getDepartment() != null)
            existing.setDepartment(
                    request.getDepartment()
            );

        if (request.getEmail() != null)
            existing.setEmail(
                    request.getEmail()
            );

        if (request.getMobileNumber() != null)
            existing.setMobileNumber(
                    request.getMobileNumber()
            );

        if (request.getGender() != null)
            existing.setGender(
                    request.getGender()
            );

        collegeStaffRepository.save(existing);

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "collegeStaffId",
                id.toString()
        );

        response.put(
                "message",
                "College Staff Details Updated Successfully"
        );

        return response;
    }


    public List<CollegeStaff> getAllCollegeStaff() {

        return collegeStaffRepository.findAll();
    }


    public CollegeStaff getCollegeStaffById(Long id) {

        return collegeStaffRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "College Staff workspace metadata not found for ID: "
                                        + id
                        )
                );
    }


    @Transactional
    public Map<String, String> approveStaffRegistration(
            Long staffId) {

        OfficeStaff staff =
                officeStaffRepository.findById(staffId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Staff not found"
                                )
                        );

        staff.setApprovalStatus("APPROVED");

        officeStaffRepository.save(staff);

        return Map.of(
                "message",
                "Staff account approved. They can now login."
        );
    }


    public List<StaffResponseDTO> getPendingStaffRegistrations() {

        List<OfficeStaff> staffs = officeStaffRepository
                .findByApprovalStatus("PENDING");

        List<StaffResponseDTO> response = new ArrayList<>();

        for(OfficeStaff staff : staffs){
            StaffResponseDTO temp = new StaffResponseDTO();
            temp.setId(staff.getId());
            temp.setEmail(staff.getEmail());
            temp.setMobileNumber(staff.getMobileNumber());
            temp.setApprovalStatus(staff.getApprovalStatus());
            temp.setBranch(staff.getBranch());
            temp.setName(staff.getName());
            temp.setRole(staff.getRole());
            response.add(temp);
        }

        return response;
    }


    public void deleteCollegeStaff(Long id) {

        if (!collegeStaffRepository.existsById(id)) {

            throw new ResourceNotFoundException(
                    "Cannot delete. College Staff mapping details missing for ID: "
                            + id
            );
        }

        collegeStaffRepository.deleteById(id);
    }


    // =========================================================
    // STUDENT GET METHODS
    // =========================================================

    public List<Student> getAllStudents() {

        return studentRepository.findAll();
    }


    public Student getStudentById(Long id) {

        return studentRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Student trace index not found for ID: "
                                        + id
                        )
                );
    }


    public Student getStudentByEmail(
            String email) {

        return studentRepository.findByEmail(email)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Student target query matching email returned empty: "
                                        + email
                        )
                );
    }


    public Student getStudentByStudentId(
            String studentId) {

        return studentRepository.findByStudentId(studentId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Student system registration ID code context not found: "
                                        + studentId
                        )
                );
    }


    // =========================================================
    // TEAM LEAD LEAVE MANAGEMENT
    // =========================================================

    @Transactional
    public Map<String, String> reviewTeamLeadLeave(
            Long adminId,
            Long leaveId,
            String status) {

        Admin admin =
                adminRepository.findById(adminId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Admin validation signature record missing for ID: "
                                                + adminId
                                )
                        );

        TeamLeadLeave leave =
                teamLeadLeaveRepository.findById(leaveId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Team Lead leave booking voucher context not found for ID: "
                                                + leaveId
                                )
                        );

        leave.setStatus(status);
        leave.setApprovedBy(admin);

        teamLeadLeaveRepository.save(leave);

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "leaveId",
                leaveId.toString()
        );

        response.put(
                "status",
                status
        );

        response.put(
                "message",
                "Team Lead Leave Request Evaluated Successfully"
        );

        return response;
    }


    public List<TeamLeadLeave>
    getAllTeamLeadLeaveRequests() {

        return teamLeadLeaveRepository.findAll();
    }


    // =========================================================
    // TEAM LEAD PERMISSION MANAGEMENT
    // =========================================================

    @Transactional
    public Map<String, String> approveTeamLeadPermission(
            Long permissionId) {

        TeamLeadPermission permission =
                teamLeadPermissionRepository
                        .findById(permissionId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Permission record not found for ID: "
                                                + permissionId
                                )
                        );

        if (!"PENDING".equalsIgnoreCase(
                permission.getStatus()
        )) {

            throw new IllegalStateException(
                    "Permission has already been processed."
            );
        }


        // =====================================================
        // DOUBLE CHECK MONTHLY QUOTA
        // Maximum 2 APPROVED permissions per month
        // =====================================================

        LocalDate permissionDate =
                permission.getPermissionDate();

        LocalDate startDate =
                permissionDate.withDayOfMonth(1);

        LocalDate endDate =
                permissionDate.withDayOfMonth(
                        permissionDate.lengthOfMonth()
                );

        long approvedCount =
                teamLeadPermissionRepository
                        .countByTeamLeadIdAndPermissionDateBetweenAndStatusIgnoreCase(
                                permission.getTeamLead().getId(),
                                startDate,
                                endDate,
                                "APPROVED"
                        );


        if (approvedCount >= 2) {

            throw new IllegalStateException(
                    "Cannot approve. Team Lead has already utilized their 2 approved monthly permissions."
            );
        }


        permission.setStatus("APPROVED");

        teamLeadPermissionRepository.save(
                permission
        );

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "permissionId",
                permissionId.toString()
        );

        response.put(
                "status",
                "APPROVED"
        );

        response.put(
                "message",
                "Team Lead permission approved successfully."
        );

        return response;
    }


    @Transactional
    public Map<String, String> rejectTeamLeadPermission(
            Long permissionId,
            String remarks) {

        TeamLeadPermission permission =
                teamLeadPermissionRepository
                        .findById(permissionId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Permission record not found for ID: "
                                                + permissionId
                                )
                        );

        if (!"PENDING".equalsIgnoreCase(
                permission.getStatus()
        )) {

            throw new IllegalStateException(
                    "Permission has already been processed."
            );
        }

        permission.setStatus("REJECTED");

        permission.setRemarks(
                remarks != null
                        ? remarks
                        : "Rejected by Admin"
        );

        teamLeadPermissionRepository.save(
                permission
        );

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "permissionId",
                permissionId.toString()
        );

        response.put(
                "status",
                "REJECTED"
        );

        response.put(
                "message",
                "Team Lead permission rejected successfully."
        );

        return response;
    }


    public List<TeamLeadPermissionResponseDTO>
    getPendingAdminPermissions() {

        List<TeamLeadPermission> request = teamLeadPermissionRepository
                .findByStatusIgnoreCase("PENDING");


        List<TeamLeadPermissionResponseDTO> response = new ArrayList<>();

        for (TeamLeadPermission permission : request) {

            TeamLeadPermissionResponseDTO dto =
                    new TeamLeadPermissionResponseDTO();

            dto.setId(permission.getId());
            dto.setTeamLeadId(permission.getTeamLead().getId());
            dto.setPermissionDate(permission.getPermissionDate());
            dto.setDurationHours(permission.getDurationHours());
            dto.setReason(permission.getReason());
            dto.setStatus(permission.getStatus());
            dto.setRemarks(permission.getRemarks());
            dto.setCreatedAt(permission.getCreatedAt());
            dto.setTeamLeadName(permission.getTeamLead().getName());
            dto.setTeamLeadBranch(permission.getTeamLead().getBranch());

            response.add(dto);
        }

        return response;
    }




    // =========================================================
    // OTP MANAGEMENT
    // =========================================================

    private final Map<String, String> otpStorage =
            new HashMap<>();


    @Transactional
    public TeamLead createTeamLeadFromToken(
            String authHeader,
            TeamLead teamLead) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            throw new RuntimeException("Token Required");
        }

        String token = authHeader.substring(7);

        String email = jwtService.extractUsername(token);

        String teamLeadId = generateTeamLeadId(teamLead.getBranch());
        teamLead.setTeamLeadId(teamLeadId);

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Invalid token");
        }

        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Admin not found")
                );

        // IMPORTANT:
        // Don't accept createdByAdmin from Postman.
        // Backend decides who created the Team Lead.
        teamLead.setCreatedByAdmin(admin);
        System.out.println("password received: " + teamLead.getPassword());

        if(teamLead.getPassword() != null && !teamLead.getPassword().isBlank()){
                teamLead.setPassword(
                    encoder.encode(
                            teamLead.getPassword()
                    )
            );
        }else{
            teamLead.setPassword(
                    encoder.encode(
                            teamLead.getEmail()
                    )
            );
        }

        return teamLeadRepository.save(teamLead);
    }


    @Transactional
    public String sendOtp(String email) {

        Admin admin =
                adminRepository.findByEmail(email)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Email Not Found"
                                )
                        );

        String otp =
                String.valueOf(
                        (int) (Math.random() * 900000)
                                + 100000
                );

        otpRepository.deleteByEmail(email);

        OtpEntity otpEntity =
                new OtpEntity();

        otpEntity.setEmail(email);
        otpEntity.setOtpCode(otp);

        otpEntity.setExpiryTime(
                LocalDateTime.now()
                        .plusMinutes(5)
        );

        otpRepository.save(otpEntity);

        emailService.sendOtpEmail(
                email,
                otp
        );

        return "OTP sent successfully to your registered email.";
    }


    public String verifyOtp(
            String email,
            String otp) {

        OtpEntity otpEntity =
                otpRepository.findByEmail(email)
                        .orElseThrow(
                                () -> new RuntimeException(
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

        if (!otpEntity.getOtpCode()
                .equals(otp)) {

            throw new RuntimeException(
                    "Invalid OTP"
            );
        }

        return "OTP Verified Successfully";
    }


    @Transactional
    public String resetPassword(
            String email,
            String otp,
            String newPassword) {

        verifyOtp(email, otp);

        Admin admin =
                adminRepository.findByEmail(email)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Email Not Found"
                                )
                        );

        admin.setPassword(
                encoder.encode(newPassword)
        );

        adminRepository.save(admin);

        otpRepository.deleteByEmail(email);

        return "Password Reset Successful";
    }


    // =========================================================
    // ADMIN DASHBOARD
    // =========================================================

    public Map<String, Object>
    getAdminDashboardStats() {

        Map<String, Object> statistics =
                new HashMap<>();

        long totalStaff =
                officeStaffRepository.count();

        long totalTeamLeads =
                teamLeadRepository.count();

        long totalStudents =
                studentRepository.count();

        long totalCollegeStaff =
                collegeStaffRepository.count();

        long totalCourses =
                courseRepository.count();

        long totalTasks =
                taskRepository.count();

        long completedTasks =
                taskRepository.countByStatus(
                        TaskStatus.COMPLETED
                );

        long pendingTasks =
                taskRepository.countByStatus(
                        TaskStatus.PENDING
                );

        long assignedTasks =
                taskRepository.countByStatus(
                        TaskStatus.ASSIGNED
                );

        long reworkTasks =
                taskRepository.countByStatus(
                        TaskStatus.REWORK_REQUIRED
                );

        long totalActivePending =
                pendingTasks
                        + assignedTasks
                        + reworkTasks;

        double taskCompletionRate =
                totalTasks > 0
                        ? ((double) completedTasks
                           / totalTasks) * 100
                        : 0.0;

        List<TeamLead> teamLeads =
                teamLeadRepository.findAll();

        double avgTeamLeadPerformance =
                teamLeads.stream()
                        .mapToDouble(
                                tl -> tl.getPerformanceScore() != null
                                        ? tl.getPerformanceScore()
                                        : 0.0
                        )
                        .average()
                        .orElse(0.0);


        statistics.put(
                "totalStaff",
                totalStaff
        );

        statistics.put(
                "totalTeamLeads",
                totalTeamLeads
        );

        statistics.put(
                "totalStudents",
                totalStudents
        );

        statistics.put(
                "totalCollegeStaff",
                totalCollegeStaff
        );

        statistics.put(
                "totalCourses",
                totalCourses
        );


        Map<String, Object> taskMetrics =
                new HashMap<>();

        taskMetrics.put(
                "total",
                totalTasks
        );

        taskMetrics.put(
                "assigned",
                assignedTasks
        );

        taskMetrics.put(
                "completed",
                completedTasks
        );

        taskMetrics.put(
                "pending",
                pendingTasks
        );

        taskMetrics.put(
                "reworkRequired",
                reworkTasks
        );

        taskMetrics.put(
                "totalActivePending",
                totalActivePending
        );

        statistics.put(
                "taskBreakdown",
                taskMetrics
        );


        Map<String, Object> analytics =
                new HashMap<>();

        analytics.put(
                "taskCompletionRatePct",
                Math.round(
                        taskCompletionRate * 100.0
                ) / 100.0
        );

        analytics.put(
                "averageTeamLeadPerformanceScore",
                Math.round(
                        avgTeamLeadPerformance * 100.0
                ) / 100.0
        );

        statistics.put(
                "performanceAnalytics",
                analytics
        );

        statistics.put(
                "systemStatus",
                "OPERATIONAL"
        );

        return statistics;
    }
}