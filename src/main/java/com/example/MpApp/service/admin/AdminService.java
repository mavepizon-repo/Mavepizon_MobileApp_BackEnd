package com.example.MpApp.service.admin;

import com.example.MpApp.dto.teamlead.TeamLeadPermissionRequestDTO;
import com.example.MpApp.entity.admin.Admin;
import com.example.MpApp.entity.collegestaff.CollegeStaff;
import com.example.MpApp.entity.enums.StaffCategory;
import com.example.MpApp.entity.officestaff.OfficeStaff;
import com.example.MpApp.entity.student.Student;
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
import com.example.MpApp.repository.student.StudentRepository;
import com.example.MpApp.repository.teamlead.TeamLeadLeaveRepository;
import com.example.MpApp.repository.teamlead.TeamLeadPermissionRepository;
import com.example.MpApp.repository.teamlead.TeamLeadRepository;
import com.example.MpApp.repository.officestaff.OfficeStaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final TeamLeadRepository teamLeadRepository;
    private final OfficeStaffRepository officeStaffRepository;
    private final CollegeStaffRepository collegeStaffRepository;
    private final StudentRepository studentRepository;
    private final TeamLeadLeaveRepository teamLeadLeaveRepository;
    private final BCryptPasswordEncoder encoder;
    private final JwtService jwtService;
    private final TeamLeadPermissionRepository teamLeadPermissionRepository;

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
}