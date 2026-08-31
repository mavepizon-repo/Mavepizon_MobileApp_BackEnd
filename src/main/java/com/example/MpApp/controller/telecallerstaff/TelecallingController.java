package com.example.MpApp.controller.telecallerstaff;

import com.example.MpApp.dto.telecallerstaff.TelecallingEnquiryRequest;
import com.example.MpApp.dto.telecallerstaff.TelecallingFollowupRequest;
import com.example.MpApp.dto.telecallerstaff.TelecallingUpdateRequest;
import com.example.MpApp.entity.enums.EnquiryStatus;
import com.example.MpApp.entity.student.Student;
import com.example.MpApp.entity.telecallerstaff.TelecallingEnquiry;
import com.example.MpApp.entity.telecallerstaff.TelecallingFollowup;
import com.example.MpApp.service.telecallerstaff.TelecallingService;
import io.swagger.v3.oas.annotations.headers.Header;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/officestaff/telecalling")
@RequiredArgsConstructor
public class TelecallingController {

    private final TelecallingService telecallingService;

    /*
     =====================================
     ENQUIRY CRUD
     =====================================
     */

    @PostMapping("/enquiry/create")
    public ResponseEntity<?> createEnquiry(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody TelecallingEnquiryRequest request) {

        return ResponseEntity.ok(
                telecallingService.createEnquiry(
                        authHeader,
                        request));
    }

    @GetMapping("/enquiry/all")
    public ResponseEntity<List<TelecallingEnquiry>> getAllEnquiries(
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(
                telecallingService.getAllEnquiries(authHeader));
    }

    @GetMapping("/enquiry/{enquiryId}")
    public ResponseEntity<TelecallingEnquiry> getEnquiryById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long enquiryId) {

        return ResponseEntity.ok(
                telecallingService.getEnquiryById(
                        authHeader,
                        enquiryId));
    }

    @PutMapping("/enquiry/update/{enquiryId}")
    public ResponseEntity<?> updateEnquiry(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long enquiryId,
            @RequestBody TelecallingUpdateRequest request) {

        return ResponseEntity.ok(
                telecallingService.updateEnquiry(
                        authHeader,
                        enquiryId,
                        request));
    }

    @DeleteMapping("/enquiry/delete/{enquiryId}")
    public ResponseEntity<String> deleteEnquiry(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long enquiryId) {

        telecallingService.deleteEnquiry(
                authHeader,
                enquiryId);

        return ResponseEntity.ok(
                "Enquiry deleted successfully");
    }

    /*
     =====================================
     FILTERS
     =====================================
     */

    @GetMapping("/enquiry/college")
    public ResponseEntity<List<TelecallingEnquiry>>
    filterByCollege(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("college") String collegeName) {

        return ResponseEntity.ok(
                telecallingService.filterByCollege(
                        authHeader,
                        collegeName));
    }

    @GetMapping("/enquiry/status")
    public ResponseEntity<List<TelecallingEnquiry>>
    filterByStatus(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam EnquiryStatus status) {

        return ResponseEntity.ok(
                telecallingService.filterByStatus(
                        authHeader,
                        status));
    }

    @GetMapping("/enquiry/student")
    public ResponseEntity<List<TelecallingEnquiry>>
    filterByStudentName(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String studentName) {

        return ResponseEntity.ok(
                telecallingService.filterByStudentName(
                        authHeader,
                        studentName));
    }

    @GetMapping("/enquiry/date")
    public ResponseEntity<List<TelecallingEnquiry>>
    filterByDate(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String date) {

        return ResponseEntity.ok(
                telecallingService.filterByDate(
                        authHeader,
                        LocalDate.parse(date)));
    }

    /*
     =====================================
     FOLLOWUPS
     =====================================
     */

    @GetMapping("/enquiry/today-followups")
    public ResponseEntity<List<TelecallingEnquiry>>
    getTodayFollowups(
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(
                telecallingService.getTodayFollowups(
                        authHeader));
    }

    @GetMapping("/custom-followups")
    public ResponseEntity<List<TelecallingFollowup>>
    getCustomFollowups(
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(
                telecallingService.getCustomFollowups(
                        authHeader));
    }

    @PostMapping("/followup/{enquiryId}")
    public ResponseEntity<?>
    addFollowup(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long enquiryId,
            @RequestBody TelecallingFollowupRequest request) {

        return ResponseEntity.ok(
                telecallingService.addFollowup(
                        authHeader,
                        enquiryId,
                        request));
    }

    @GetMapping("/history/{enquiryId}")
    public ResponseEntity<List<TelecallingFollowup>>
    getFollowupHistory(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long enquiryId) {

        return ResponseEntity.ok(
                telecallingService.getFollowupHistory(
                        authHeader,
                        enquiryId));
    }

    /*
     =====================================
     ENQUIRY -> STUDENT
     =====================================
     */

    @PostMapping("/update/")
    public ResponseEntity<?> updateEnquiryStatus(
            @PathVariable EnquiryStatus status,
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(
                telecallingService.updateEnquiryStatus(
                        status,
                        authHeader));
    }
}