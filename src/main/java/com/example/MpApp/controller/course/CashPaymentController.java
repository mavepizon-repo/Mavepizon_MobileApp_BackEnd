package com.example.MpApp.controller.course;

import com.example.MpApp.config.JwtService;
import com.example.MpApp.dto.course.CashPaymentRequest;
import com.example.MpApp.entity.admin.Admin;
import com.example.MpApp.entity.course.CashPayment;
import com.example.MpApp.entity.teamlead.TeamLead;
import com.example.MpApp.exception.InvalidCredentialsException;
import com.example.MpApp.exception.JwtAuthenticationException;
import com.example.MpApp.repository.admin.AdminRepository;
import com.example.MpApp.repository.teamlead.TeamLeadRepository;
import com.example.MpApp.service.course.CashPaymentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cash-payment")
@CrossOrigin("*")
public class CashPaymentController {

    @Autowired
    private CashPaymentService service;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private TeamLeadRepository teamLeadRepository;

    /**
     * Validate Admin or Team Lead
     */
    private String validateAdminOrTeamLead(String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new JwtAuthenticationException(
                    "Access Denied: Missing or invalid token authentication scheme.");
        }

        String token = authHeader.substring(7);
        String email;

        try {
            email = jwtService.extractUsername(token);
        } catch (Exception e) {
            throw new JwtAuthenticationException(
                    "Access Denied: Malformed or expired authentication token.");
        }

        if (email == null) {
            throw new InvalidCredentialsException(
                    "Access Denied: Token contains no identification principal.");
        }

        Admin admin = adminRepository.findByEmail(email).orElse(null);
        if (admin != null) {
            return "ADMIN";
        }

        TeamLead teamLead = teamLeadRepository.findByEmail(email).orElse(null);
        if (teamLead != null) {
            return "TEAM_LEAD";
        }

        throw new InvalidCredentialsException(
                "Access Denied: User account is not verified as Admin or Team Lead.");
    }

    /**
     * Basic Token Validation
     */
    private String validateToken(String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new JwtAuthenticationException(
                    "Access Denied: Missing or invalid token authentication scheme.");
        }

        String token = authHeader.substring(7);

        try {
            return jwtService.extractUsername(token);
        } catch (Exception e) {
            throw new JwtAuthenticationException(
                    "Access Denied: Invalid or expired authentication token.");
        }
    }

    // ==========================================================
    // CREATE CASH REQUEST
    // ==========================================================

    @PostMapping("/create")
    public ResponseEntity<?> createCashRequest(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CashPaymentRequest request) {

        validateToken(authHeader);

        String token = authHeader.substring(7);

        return ResponseEntity.ok(
                service.createCashRequest(token, request));
    }

    // ==========================================================
    // APPROVE CASH PAYMENT
    // Developer Trainer authorization is handled in Service
    // ==========================================================

    @PutMapping("/approve/{id}")
    public ResponseEntity<?> approvePayment(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        validateToken(authHeader);

        String token = authHeader.substring(7);

        return ResponseEntity.ok(
                service.approveCashPayment(id, token));
    }

    // ==========================================================
    // REJECT CASH PAYMENT
    // Developer Trainer authorization is handled in Service
    // ==========================================================

    @PutMapping("/reject/{id}")
    public ResponseEntity<?> rejectPayment(
            @PathVariable Long id,
            @RequestParam String remarks,
            @RequestHeader("Authorization") String authHeader) {

        validateToken(authHeader);

        String token = authHeader.substring(7);

        return ResponseEntity.ok(
                service.rejectCashPayment(id, remarks, token));
    }

    // ==========================================================
    // GET ALL PAYMENTS
    // ADMIN / TEAM LEAD ONLY
    // ==========================================================

    @GetMapping("/get-all")
    public ResponseEntity<List<CashPayment>> getAllPayments(
            @RequestHeader("Authorization") String authHeader) {

        validateAdminOrTeamLead(authHeader);

        return ResponseEntity.ok(
                service.getAllCashPayments());
    }

    // ==========================================================
    // GET PAYMENT BY ID
    // ==========================================================

    @GetMapping("/get/{id}")
    public ResponseEntity<CashPayment> getById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        validateToken(authHeader);

        return ResponseEntity.ok(
                service.getCashPaymentById(id));
    }

    // ==========================================================
    // GET PAYMENTS BY STATUS
    // ADMIN / TEAM LEAD ONLY
    // ==========================================================

    @GetMapping("/status/{status}")
    public ResponseEntity<List<CashPayment>> getByStatus(
            @PathVariable String status,
            @RequestHeader("Authorization") String authHeader) {

        validateAdminOrTeamLead(authHeader);

        return ResponseEntity.ok(
                service.getByStatus(status));
    }

    // ==========================================================
    // MY PAYMENTS
    // ==========================================================

    @GetMapping("/my-payments")
    public ResponseEntity<List<CashPayment>> myPayments(
            @RequestHeader("Authorization") String authHeader) {

        validateToken(authHeader);

        String token = authHeader.substring(7);

        return ResponseEntity.ok(
                service.getMyPayments(token));
    }

    // ==========================================================
    // PAYMENTS ASSIGNED TO STAFF
    // ADMIN / TEAM LEAD ONLY
    // ==========================================================

    @GetMapping("/staff/{staffId}")
    public ResponseEntity<List<CashPayment>> getByStaff(
            @PathVariable Long staffId,
            @RequestHeader("Authorization") String authHeader) {

        validateAdminOrTeamLead(authHeader);

        return ResponseEntity.ok(
                service.getByStaff(staffId));
    }

    // ==========================================================
    // DELETE PAYMENT
    // ADMIN / TEAM LEAD ONLY
    // ==========================================================

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deletePayment(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        validateAdminOrTeamLead(authHeader);

        service.deleteCashPayment(id);

        return ResponseEntity.ok(
                "Cash Payment Deleted Successfully");
    }
}