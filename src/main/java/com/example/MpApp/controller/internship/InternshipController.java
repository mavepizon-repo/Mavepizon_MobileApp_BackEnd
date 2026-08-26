package com.example.MpApp.controller.internship;

import com.example.MpApp.entity.internship.Internship;
import com.example.MpApp.entity.internship.InternshipRegistration;
import com.example.MpApp.exception.InvalidCredentialsException;
import com.example.MpApp.exception.JwtAuthenticationException;
import com.example.MpApp.service.internship.InternshipService;
import com.example.MpApp.config.JwtService;

import com.example.MpApp.entity.admin.Admin;
import com.example.MpApp.entity.teamlead.TeamLead;
import com.example.MpApp.repository.admin.AdminRepository;
import com.example.MpApp.repository.teamlead.TeamLeadRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/internships")
@RequiredArgsConstructor
@CrossOrigin("*")
public class InternshipController {

    private final InternshipService internshipService;
    private final JwtService jwtService;
    private final AdminRepository adminRepository;
    private final TeamLeadRepository teamLeadRepository;

    /**
     * Validates token and checks DB for Admin or Team Lead status.
     * Returns a String representing the matching role.
     */
    private String validateAdminOrTeamLead(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new JwtAuthenticationException("Access Denied: Missing or invalid token authentication scheme.");
        }

        String token = authHeader.substring(7);
        String email;

        try {
            email = jwtService.extractUsername(token); // Using extractUsername based on your template
        } catch (Exception e) {
            throw new JwtAuthenticationException("Access Denied: Malformed or expired authentication token.");
        }

        if (email == null) {
            throw new InvalidCredentialsException("Access Denied: Token contains no identification principal.");
        }

        // Check Admin Table
        Admin admin = adminRepository.findByEmail(email).orElse(null);
        if (admin != null) {
            return "ADMIN";
        }

        // Check Team Lead Table
        TeamLead teamLead = teamLeadRepository.findByEmail(email).orElse(null);
        if (teamLead != null) {
            return "TEAM_LEAD";
        }

        throw new InvalidCredentialsException("Access Denied: User account is not verified as Admin or Team Lead.");
    }

    // ================= CONTROLLER ENDPOINTS =================

    @PostMapping
    public ResponseEntity<?> createInternship(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Internship internship,
            @RequestParam(value = "location", required = false, defaultValue = "ONLINE") String location) {

        String creatorRole = validateAdminOrTeamLead(authHeader);
        return ResponseEntity.ok(internshipService.createInternship(internship, creatorRole));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateInternship(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestBody Internship details) {

        validateAdminOrTeamLead(authHeader);
        return ResponseEntity.ok(internshipService.updateInternship(id, details));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerStudent(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody InternshipRegistration registration) {

        // Kept intact if student route validation needs its own DB-check table lookup later
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new JwtAuthenticationException("Access Denied: Missing or invalid token authentication scheme.");
        }
        return ResponseEntity.ok(internshipService.registerStudent(registration));
    }

    @GetMapping
    public ResponseEntity<List<Internship>> getAllInternships(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new JwtAuthenticationException("Access Denied: Missing or invalid token authentication scheme.");
        }
        return ResponseEntity.ok(internshipService.getAllInternships());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Internship> getInternshipById(
            @PathVariable Long id) {


        return ResponseEntity.ok(
                internshipService.getInternshipById(id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInternship(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {

        validateAdminOrTeamLead(authHeader);

        internshipService.deleteInternship(id);

        return ResponseEntity.ok(
                "Internship Deleted Successfully"
        );
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleInternshipStatus(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {

        // Restrict access to Admins or Team Leads
        validateAdminOrTeamLead(authHeader);

        return ResponseEntity.ok(internshipService.toggleInternshipStatus(id));
    }
}