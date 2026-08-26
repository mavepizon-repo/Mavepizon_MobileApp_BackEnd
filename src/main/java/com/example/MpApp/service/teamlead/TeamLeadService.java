package com.example.MpApp.service.teamlead;

import com.example.MpApp.dto.officestaff.CreatedByDTO;
import com.example.MpApp.dto.officestaff.LeaveRequestDTO;
import com.example.MpApp.dto.officestaff.OfficeStaffResponseDTO;
import com.example.MpApp.dto.task.*;
import com.example.MpApp.dto.teamlead.TeamLeadLoginRequest;
import com.example.MpApp.dto.teamlead.TeamLeadPermissionRequestDTO;
import com.example.MpApp.dto.teamlead.TeamLeadProfileDTO;
import com.example.MpApp.entity.OtpEntity;
import com.example.MpApp.entity.admin.Admin;
import com.example.MpApp.entity.collegestaff.CollegeStaff;
import com.example.MpApp.entity.enums.StaffCategory;
import com.example.MpApp.entity.officestaff.OfficeStaff;
import com.example.MpApp.entity.officestaff.OfficeStaffLeave;
import com.example.MpApp.entity.officestaff.OfficeStaffPermission;
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
import com.example.MpApp.repository.officestaff.OfficeStaffLeaveRepository;
import com.example.MpApp.repository.officestaff.OfficeStaffPermissionRepository;
import com.example.MpApp.repository.officestaff.OfficeStaffRepository;
import com.example.MpApp.repository.task.TaskRepository;
import com.example.MpApp.repository.task.TaskReviewRepository;
import com.example.MpApp.repository.teamlead.TeamLeadLeaveRepository;
import com.example.MpApp.repository.teamlead.TeamLeadPermissionRepository;
import com.example.MpApp.repository.teamlead.TeamLeadRepository;
import com.example.MpApp.config.JwtService;
import com.example.MpApp.entity.enums.TaskStatus;
import com.example.MpApp.entity.enums.VerificationStatus;
import com.example.MpApp.repository.collegestaff.CollegeStaffRepository;
import com.example.MpApp.repository.student.StudentRepository;

import com.example.MpApp.service.CloudinaryService;
import com.example.MpApp.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
public class TeamLeadService {

    private final TeamLeadRepository repository;
    private final OfficeStaffRepository officeStaffRepository;
    private final TaskRepository taskRepository;
    private final TaskReviewRepository taskReviewRepository;
    private final StudentRepository studentRepository;
    private final JwtService jwtService;
    private final CollegeStaffRepository collegeStaffRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final OfficeStaffLeaveRepository officeStaffLeaveRepository;
    private final TeamLeadLeaveRepository teamLeadLeaveRepository;
    private final OfficeStaffPermissionRepository permissionRepository;
    private final TeamLeadPermissionRepository teamLeadPermissionRepository;
    private final OtpRepository otpRepository;
    private final AdminRepository adminRepository;


    private final CloudinaryService cloudinaryService;
    private final TeamLeadAttendanceService teamLeadAttendanceService;
    private final EmailService emailService;

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

    public String extractEmail(String authHeader){
        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            throw new RuntimeException("Token Required");
        }

        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);

        return email;
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

    public Map<String, String> loginTeamLead(TeamLeadLoginRequest request) {
        TeamLead teamLead = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead not found for email: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), teamLead.getPassword())) {
            throw new InvalidCredentialsException("Invalid password. Please try again.");
        }

        UserDetails userDetails = User.builder()
                .username(teamLead.getEmail())
                .password(teamLead.getPassword())
                .roles("TEAM_LEAD")
                .build();

        String token = jwtService.generateToken(userDetails);

        Map<String, String> response = new HashMap<>();
        response.put("teamLeadId", teamLead.getId().toString());
        response.put("name", teamLead.getName());
        response.put("email", teamLead.getEmail());
        response.put("token", token);
        response.put("message", "Login Successful");

        return response;
    }


    // ---------------- TASK ASSIGNMENT (UPDATED) ----------------

    @Transactional
    public Map<String, String> assignTask(String authHeader, TaskRequest request) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            throw new RuntimeException("Token Required");
        }

        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);

        OfficeStaff staff = officeStaffRepository.findById(request.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found for ID: " + request.getStaffId()));

        TeamLead lead = repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead not found for email : " + email));

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

    public Map<String , String> assignTaskToSome(String authHeader , TaskRequestSome request){

        String email = extractEmail(authHeader);

        TeamLead teamLead = repository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("Team lead not found for email : " + email)
        );

        List<Long> staffIds = request.getStaffIds();

        if(staffIds.isEmpty()) throw new IllegalArgumentException("Staff ID is Empty");

        for(Long staffId : staffIds){

            OfficeStaff staff = officeStaffRepository.findById(staffId).orElseThrow(
                    ()-> new ResourceNotFoundException("Staff not found for ID : " + staffId)
            );

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
            task.setTeamLead(teamLead);

            taskRepository.save(task);



        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Tasks Assigned Successfully");
        return response;

    }

    //ASSIGN WORK TO ALL STAFF
    public Map<String,String> assignWorkToAllStaff(String authHeader , TaskRequest request) {
        String email = extractEmail(authHeader);
        TeamLead teamLead = repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead not found for email : " + email));

        List<OfficeStaff> allStaff = officeStaffRepository.findByCreatedByIdAndType(teamLead.getId(),"TEAM_LEAD");

        for(OfficeStaff staff : allStaff){

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
            task.setTeamLead(teamLead);

            taskRepository.save(task);

        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Tasks Assigned Successfully");
        return response;
    }

    // ---------------- STAFF MANAGEMENT (UPDATED) ----------------

    @Transactional
    public Map<String, String> createStaff(String authHeader, OfficeStaff staff) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            throw new RuntimeException("Token Required");
        }

        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);
        String role = getMyRole();

        System.out.println("Role : " + role);



        if(role.equals("ROLE_TEAM_LEAD")){
            TeamLead teamLead = repository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Team Lead not found for Email: " + email));
            staff.setCreatedBy(teamLead);

        }else if(role.equals("ROLE_ADMIN")){

            Admin admin = adminRepository.findByEmail(email).orElseThrow(
                    () -> new ResourceNotFoundException("Admin not found for Email: " + email)
            );

            staff.setCreatedBy(admin);
            staff.setApprovalStatus("APPROVED");

        }



        if (officeStaffRepository.findByEmail(staff.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email Already Exists: " + staff.getEmail());
        }

        String generatedStaffId = generateStaffId(staff.getBranch(), staff.getCategory());
        staff.setStaffId(generatedStaffId);
        staff.setPassword(passwordEncoder.encode(staff.getPassword()));
        staff.setActive(true);

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
    public Map<String, String> updateStaff(String authHeader, Long staffId, OfficeStaff request) {

        String email = extractEmail(authHeader);
        repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead not found for ID: " + email));

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

    // GET ALL STAFFS
    public List<OfficeStaffResponseDTO> getAllStaff() {

        List<OfficeStaff> staffs = officeStaffRepository.findAll();
        List<OfficeStaffResponseDTO> response = new ArrayList<>();


            for (OfficeStaff staff : staffs) {

                OfficeStaffResponseDTO dto = new OfficeStaffResponseDTO();

                dto.setId(staff.getId());
                dto.setStaffId(staff.getStaffId());
                dto.setEmployeeId(staff.getEmployeeId());

                dto.setName(staff.getName());
                dto.setEmail(staff.getEmail());
                dto.setMobileNumber(staff.getMobileNumber());

                dto.setGender(staff.getGender());
                dto.setBloodGroup(staff.getBloodGroup());

                dto.setBranch(staff.getBranch());
                dto.setBranchName(staff.getBranchName());

                dto.setCategory(
                        staff.getCategory() != null
                                ? staff.getCategory().name()
                                : null
                );

                dto.setRole(staff.getRole());

                dto.setDegree(staff.getDegree());
                dto.setYearPassedOut(staff.getYearPassedOut());
                dto.setJoiningDate(staff.getJoiningDate());

                dto.setNativePlace(staff.getNativePlace());

                dto.setExperience(staff.getExperience());
                dto.setPreviousCompany(staff.getPreviousCompany());
                dto.setSkills(staff.getSkills());

                dto.setAadhaarFile(staff.getAadhaarFile());
                dto.setProfilePhoto(staff.getProfilePhoto());
                dto.setResumeFile(staff.getResumeFile());
                dto.setExperienceCertificate(staff.getExperienceCertificate());

                dto.setScore(staff.getScore());

                dto.setActive(staff.isActive());
                dto.setApprovalStatus(staff.getApprovalStatus());

                // createdBy
                if (staff.getCreatedBy() instanceof TeamLead teamLead) {

                    CreatedByDTO createdByDTO = new CreatedByDTO();

                    createdByDTO.setId(teamLead.getId());
                    createdByDTO.setName(teamLead.getName());
                    createdByDTO.setEmail(teamLead.getEmail());
                    createdByDTO.setTeamLeadId(teamLead.getTeamLeadId());
                    createdByDTO.setType("TEAM_LEAD");

                    dto.setCreatedBy(createdByDTO);

                } else if (staff.getCreatedBy() instanceof Admin admin) {

                    CreatedByDTO createdByDTO = new CreatedByDTO();

                    createdByDTO.setId(admin.getId());
                    createdByDTO.setName(admin.getUserName());
                    createdByDTO.setEmail(admin.getEmail());
                    createdByDTO.setType("ADMIN");

                    dto.setCreatedBy(createdByDTO);
                }

                response.add(dto);
            }

            return response;

    }

    //GET ALL STAFFS BY TEAM LEAD
    public List<OfficeStaffResponseDTO> getAllStaffByTeamLead(String authHeader) {
        String email = extractEmail(authHeader);
        String role = getMyRole();
        List<OfficeStaff> staffs;
        if(role.equals("ROLE_ADMIN")){
            Admin admin = adminRepository.findByEmail(email).orElseThrow(
                    () -> new ResourceNotFoundException("Admin not found for Email: " + email)
            );

            staffs = officeStaffRepository.findByCreatedByIdAndType(admin.getId(),"ADMIN");

        }else{
            TeamLead teamLead = repository.findByEmail(email).orElseThrow(
                    () -> new ResourceNotFoundException("Team Lead not found for Email: " + email)
            );

            staffs = officeStaffRepository.findByCreatedByIdAndType(teamLead.getId(),"TEAM_LEAD");
        }

        List<OfficeStaffResponseDTO> response = new ArrayList<>();

        for(OfficeStaff staff : staffs){
            OfficeStaffResponseDTO dto = new OfficeStaffResponseDTO();

            dto.setId(staff.getId());
            dto.setStaffId(staff.getStaffId());
            dto.setEmployeeId(staff.getEmployeeId());

            dto.setName(staff.getName());
            dto.setEmail(staff.getEmail());
            dto.setMobileNumber(staff.getMobileNumber());

            dto.setGender(staff.getGender());
            dto.setBloodGroup(staff.getBloodGroup());

            dto.setBranch(staff.getBranch());
            dto.setBranchName(staff.getBranchName());

            dto.setCategory(
                    staff.getCategory() != null
                            ? staff.getCategory().name()
                            : null
            );

            dto.setRole(staff.getRole());

            dto.setDegree(staff.getDegree());
            dto.setYearPassedOut(staff.getYearPassedOut());
            dto.setJoiningDate(staff.getJoiningDate());

            dto.setNativePlace(staff.getNativePlace());

            dto.setExperience(staff.getExperience());
            dto.setPreviousCompany(staff.getPreviousCompany());
            dto.setSkills(staff.getSkills());

            dto.setAadhaarFile(staff.getAadhaarFile());
            dto.setProfilePhoto(staff.getProfilePhoto());
            dto.setResumeFile(staff.getResumeFile());
            dto.setExperienceCertificate(staff.getExperienceCertificate());

            dto.setScore(staff.getScore());

            dto.setActive(staff.isActive());
            dto.setApprovalStatus(staff.getApprovalStatus());

            // createdBy
            if (staff.getCreatedBy() instanceof TeamLead teamLead) {

                CreatedByDTO createdByDTO = new CreatedByDTO();

                createdByDTO.setId(teamLead.getId());
                createdByDTO.setName(teamLead.getName());
                createdByDTO.setEmail(teamLead.getEmail());
                createdByDTO.setTeamLeadId(teamLead.getTeamLeadId());
                createdByDTO.setType("TEAM_LEAD");

                dto.setCreatedBy(createdByDTO);

            } else if (staff.getCreatedBy() instanceof Admin admin) {

                CreatedByDTO createdByDTO = new CreatedByDTO();

                createdByDTO.setId(admin.getId());
                createdByDTO.setName(admin.getUserName());
                createdByDTO.setEmail(admin.getEmail());
                createdByDTO.setType("ADMIN");

                dto.setCreatedBy(createdByDTO);
            }

            response.add(dto);
        }


        return response;
    }



    @Transactional
    public void deleteStaff(Long staffId) {

        OfficeStaff staff = officeStaffRepository.findById(staffId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Staff not found"));

        // Delete permissions
        permissionRepository.deleteByStaffId(staffId);

        // Delete leave requests
        officeStaffLeaveRepository.deleteByStaffId(staffId);
        // Delete tasks
        taskRepository.deleteByStaffId(staffId);

        // Finally delete staff
        officeStaffRepository.delete(staff);
    }

    // ---------------- TASK MANAGEMENT (UPDATED) ----------------


    @Transactional
    public Map<String, String> updateTaskAdmin(Long taskId,String authHeader, TaskAdminUpdateRequest request) {
        String email = extractEmail(authHeader);
        TeamLead teamLead = repository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("Team Lead not found for email : " + email)
        );
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Target task context not found for ID: " + taskId));

        // Dynamically overwrite configurations if present in the update payload
        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getDeadline() != null) task.setDeadline(request.getDeadline());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getEstimatedHours() != null) task.setEstimatedHours(request.getEstimatedHours());
        if (request.getRemarks() != null) task.setRemarks(request.getRemarks());
        if (request.getCompletionRemarks() != null) task.setCompletionRemarks(request.getCompletionRemarks());
        if (request.getTaskType() != null) task.setTaskType(request.getTaskType());
        task.setTeamLead(teamLead);

        taskRepository.save(task);

        Map<String, String> response = new HashMap<>();
        response.put("message", "All structural task metadata fields updated successfully.");
        return response;
    }

    @Transactional
    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found for ID: " + taskId));
        taskRepository.delete(task);
    }

    //GET TASKS CREATED BY TEAM LEAD
    public List<TaskResponse> getAllTasksByTeamLead(String authHeader) {
        String email = extractEmail(authHeader);
        TeamLead teamLead = repository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("Team Lead not found for email : " + email)
        );

        return taskRepository.findTasksByTeamLead(teamLead.getId());


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
    public Map<String, String> reviewTask(Long taskId,String authHeader, TaskReviewRequest request) {


        String email = extractEmail(authHeader);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found for ID: " + taskId));

        TeamLead lead = repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead not found for ID: " + email));

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
    public Map<String, String> requestPermissionToAdmin(
            Long teamLeadId,
            TeamLeadPermissionRequestDTO dto) {

        // 1. Find Team Lead
        TeamLead teamLead = repository.findById(teamLeadId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team Lead record not found for ID: " + teamLeadId));

        // 2. Validate permission date
        if (dto.getPermissionDate() == null) {
            throw new IllegalArgumentException("Permission date is required");
        }

        // 3. Calculate the calendar month range
        LocalDate permissionDate = dto.getPermissionDate();
        LocalDate startDate = permissionDate.withDayOfMonth(1);
        LocalDate endDate = permissionDate.withDayOfMonth(permissionDate.lengthOfMonth());

        // 4. Count already APPROVED permissions for this Team Lead
        //    in the requested calendar month
        long approvedCount = teamLeadPermissionRepository
                .countByTeamLeadIdAndPermissionDateBetweenAndStatusIgnoreCase(
                        teamLeadId,
                        startDate,
                        endDate,
                        "APPROVED"
                );

        // 5. Maximum 2 approved permissions per month
        if (approvedCount >= 2) {
            throw new IllegalStateException(
                    "Monthly quota exceeded. Team Lead in the "
                            + teamLead.getBranch()
                            + " branch is limited to a maximum of "
                            + "2 approved permissions per calendar month."
            );
        }

        // 6. Create a new PENDING permission request
        TeamLeadPermission permission = new TeamLeadPermission();
        permission.setTeamLead(teamLead);
        permission.setPermissionDate(permissionDate);
        permission.setDurationHours(dto.getDurationHours());
        permission.setReason(dto.getReason());
        permission.setStatus("PENDING");

        // 7. Save request
        teamLeadPermissionRepository.save(permission);

        // 8. Response
        Map<String, String> response = new HashMap<>();
        response.put("status", "PENDING");
        response.put(
                "message",
                "Permission request submitted to Admin successfully."
        );

        return response;
    }

    // Inside TeamLeadService.java

    public List<TeamLeadLeave> getMyLeaveHistory(Long teamLeadId) {
        return teamLeadLeaveRepository.findByTeamLeadId(teamLeadId);
    }

    public List<TeamLeadPermission> getMyPermissionHistory(Long teamLeadId) {
        return teamLeadPermissionRepository.findByTeamLeadId(teamLeadId);
    }
    // Inject your required repositories (e.g., teamLeadRepository, officeStaffRepository)

    // Add this map at the class level of TeamLeadService
    private final Map<String, String> otpStorage = new HashMap<>();

    @Transactional
    public String sendOtp(String email) {
        TeamLead leader = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email Not Found"));

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

        // Clear existing to ensure only the latest OTP is valid
        otpRepository.deleteByEmail(email);

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
        // Verify OTP via the same logic or a shared helper
        verifyOtp(email, otp);

        TeamLead leader = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email Not Found"));

        leader.setPassword(passwordEncoder.encode(newPassword));
        repository.save(leader);

        // OTP is consumed upon successful reset
        otpRepository.deleteByEmail(email);
        return "Password Reset Successful";
    }


    public String changePassword(String email, String oldPassword, String newPassword) {
        // 1. Find the Team Lead
        TeamLead teamLead = repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead not found for email: " + email));

        // 2. Verify the old password
        if (!passwordEncoder.matches(oldPassword, teamLead.getPassword())) {
            throw new InvalidCredentialsException("Invalid Old Password");
        }

        // 3. Encrypt and set the new password
        teamLead.setPassword(passwordEncoder.encode(newPassword));
        repository.save(teamLead);

        return "Password Changed Successfully";
    }

    @Transactional
    public Map<String, String> changeStaffStatus(Long staffId, boolean active) {
        OfficeStaff staff = officeStaffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
        staff.setActive(active);
        officeStaffRepository.save(staff);
        return Map.of("message", "Staff account state updated to " + (active ? "ACTIVE" : "INACTIVE"));
    }

    public OfficeStaffResponseDTO getStaffById(String authHeader, Long staffId) {

        String email = extractEmail(authHeader);
        // 1. Verify Team Lead exists
        repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead not found for Email: " + email));

        // 2. Fetch and return the staff member
        OfficeStaff staff =  officeStaffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found for ID: " + staffId));

        OfficeStaffResponseDTO dto = new OfficeStaffResponseDTO();

        dto.setId(staff.getId());
        dto.setStaffId(staff.getStaffId());
        dto.setEmployeeId(staff.getEmployeeId());

        dto.setName(staff.getName());
        dto.setEmail(staff.getEmail());
        dto.setMobileNumber(staff.getMobileNumber());

        dto.setGender(staff.getGender());
        dto.setBloodGroup(staff.getBloodGroup());

        dto.setBranch(staff.getBranch());
        dto.setBranchName(staff.getBranchName());

        dto.setCategory(
                staff.getCategory() != null
                        ? staff.getCategory().name()
                        : null
        );

        dto.setRole(staff.getRole());

        dto.setDegree(staff.getDegree());
        dto.setYearPassedOut(staff.getYearPassedOut());
        dto.setJoiningDate(staff.getJoiningDate());

        dto.setNativePlace(staff.getNativePlace());

        dto.setExperience(staff.getExperience());
        dto.setPreviousCompany(staff.getPreviousCompany());
        dto.setSkills(staff.getSkills());

        dto.setAadhaarFile(staff.getAadhaarFile());
        dto.setProfilePhoto(staff.getProfilePhoto());
        dto.setResumeFile(staff.getResumeFile());
        dto.setExperienceCertificate(staff.getExperienceCertificate());

        dto.setScore(staff.getScore());

        dto.setActive(staff.isActive());
        dto.setApprovalStatus(staff.getApprovalStatus());

        // createdBy
        if (staff.getCreatedBy() instanceof TeamLead teamLead) {

            CreatedByDTO createdByDTO = new CreatedByDTO();

            createdByDTO.setId(teamLead.getId());
            createdByDTO.setName(teamLead.getName());
            createdByDTO.setEmail(teamLead.getEmail());
            createdByDTO.setTeamLeadId(teamLead.getTeamLeadId());
            createdByDTO.setType("TEAM_LEAD");

            dto.setCreatedBy(createdByDTO);

        } else if (staff.getCreatedBy() instanceof Admin admin) {

            CreatedByDTO createdByDTO = new CreatedByDTO();

            createdByDTO.setId(admin.getId());
            createdByDTO.setName(admin.getUserName());
            createdByDTO.setEmail(admin.getEmail());
            createdByDTO.setType("ADMIN");

            dto.setCreatedBy(createdByDTO);
        }

        return dto;


    }

    public TeamLeadProfileDTO getTeamLeadProfile(Long teamLeadId) {
        TeamLead tl = repository.findById(teamLeadId)
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead not found for ID: " + teamLeadId));

        TeamLeadProfileDTO dto = new TeamLeadProfileDTO();
        dto.setId(tl.getId());
        dto.setName(tl.getName());
        dto.setTeamLeadId(tl.getTeamLeadId());
        dto.setBranch(tl.getBranch());
        dto.setEmail(tl.getEmail());
        dto.setMobileNumber(tl.getMobileNumber());
        dto.setRole(tl.getRole());
        dto.setProfilePhoto(tl.getProfilePhoto());
        dto.setPerformanceScore(tl.getScore());
        dto.setActive(tl.getActive());

        return dto;
    }

    public Map<String, Object> getTeamLeadDashboardStats(Long teamLeadId) {
        Map<String, Object> stats = new HashMap<>();

        // 1. Task Analytics
        long totalTasks = taskRepository.countByTeamLeadId(teamLeadId);
        long completedTasks = taskRepository.countByTeamLeadIdAndStatus(teamLeadId, TaskStatus.COMPLETED);
        long pendingTasks = totalTasks - completedTasks;

        // 2. Batch Analytics
        // TrainingBatch is no longer managed by TeamLeadService.
        // Course creation and staff assignment are handled by
        // CourseService and CourseStaffAssignmentService.

        // 3. Attendance Summary (using your AttendanceService)
        double attendancePercent = teamLeadAttendanceService.calculateAttendancePercentage(teamLeadId);

        stats.put("totalTasks", totalTasks);
        stats.put("completedTasks", completedTasks);
        stats.put("pendingTasks", pendingTasks);
        stats.put("attendancePercentage", attendancePercent);
        stats.put("systemStatus", "OPERATIONAL");

        return stats;
    }
}