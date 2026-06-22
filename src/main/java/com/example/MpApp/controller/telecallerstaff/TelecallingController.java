package com.example.MpApp.controller.telecallerstaff;

import com.example.MpApp.dto.telecallerstaff.TelecallingEnquiryRequest;
import com.example.MpApp.dto.telecallerstaff.TelecallingFollowupRequest;
import com.example.MpApp.dto.telecallerstaff.TelecallingUpdateRequest;
import com.example.MpApp.entity.enums.EnquiryStatus;
import com.example.MpApp.entity.student.Student;
import com.example.MpApp.entity.telecallerstaff.TelecallingEnquiry;
import com.example.MpApp.entity.telecallerstaff.TelecallingFollowup;
import com.example.MpApp.service.telecallerstaff.TelecallingService;
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

    @PostMapping("/{staffId}/enquiry")
    public ResponseEntity<?> createEnquiry(
            @PathVariable Long staffId,
            @RequestBody TelecallingEnquiryRequest request) {

        return ResponseEntity.ok(
                telecallingService.createEnquiry(
                        staffId,
                        request));
    }

    @GetMapping("/{staffId}/enquiries")
    public ResponseEntity<List<TelecallingEnquiry>> getAllEnquiries(
            @PathVariable Long staffId) {

        return ResponseEntity.ok(
                telecallingService.getAllEnquiries(
                        staffId));
    }

    @GetMapping("/{staffId}/enquiry/{enquiryId}")
    public ResponseEntity<TelecallingEnquiry> getEnquiryById(
            @PathVariable Long staffId,
            @PathVariable Long enquiryId) {

        return ResponseEntity.ok(
                telecallingService.getEnquiryById(
                        staffId,
                        enquiryId));
    }

    @PutMapping("/{staffId}/enquiry/{enquiryId}")
    public ResponseEntity<?> updateEnquiry(
            @PathVariable Long staffId,
            @PathVariable Long enquiryId,
            @RequestBody TelecallingUpdateRequest request) {

        return ResponseEntity.ok(
                telecallingService.updateEnquiry(
                        staffId,
                        enquiryId,
                        request));
    }

    @DeleteMapping("/{staffId}/enquiry/{enquiryId}")
    public ResponseEntity<String> deleteEnquiry(
            @PathVariable Long staffId,
            @PathVariable Long enquiryId) {

        telecallingService.deleteEnquiry(
                staffId,
                enquiryId);

        return ResponseEntity.ok(
                "Enquiry deleted successfully");
    }

    /*
     =====================================
     FILTERS
     =====================================
     */

    @GetMapping("/{staffId}/college")
    public ResponseEntity<List<TelecallingEnquiry>>
    filterByCollege(
            @PathVariable Long staffId,
            @RequestParam String collegeName) {

        return ResponseEntity.ok(
                telecallingService.filterByCollege(
                        staffId,
                        collegeName));
    }

    @GetMapping("/{staffId}/status")
    public ResponseEntity<List<TelecallingEnquiry>>
    filterByStatus(
            @PathVariable Long staffId,
            @RequestParam EnquiryStatus status) {

        return ResponseEntity.ok(
                telecallingService.filterByStatus(
                        staffId,
                        status));
    }

    @GetMapping("/{staffId}/student")
    public ResponseEntity<List<TelecallingEnquiry>>
    filterByStudentName(
            @PathVariable Long staffId,
            @RequestParam String studentName) {

        return ResponseEntity.ok(
                telecallingService.filterByStudentName(
                        staffId,
                        studentName));
    }

    @GetMapping("/{staffId}/date")
    public ResponseEntity<List<TelecallingEnquiry>>
    filterByDate(
            @PathVariable Long staffId,
            @RequestParam String date) {

        return ResponseEntity.ok(
                telecallingService.filterByDate(
                        staffId,
                        LocalDate.parse(date)));
    }

    /*
     =====================================
     FOLLOWUPS
     =====================================
     */

    @GetMapping("/{staffId}/today-followups")
    public ResponseEntity<List<TelecallingFollowup>>
    getTodayFollowups(
            @PathVariable Long staffId) {

        return ResponseEntity.ok(
                telecallingService.getTodayFollowups(
                        staffId));
    }

    @GetMapping("/{staffId}/custom-followups")
    public ResponseEntity<List<TelecallingFollowup>>
    getCustomFollowups(
            @PathVariable Long staffId) {

        return ResponseEntity.ok(
                telecallingService.getCustomFollowups(
                        staffId));
    }

    @PostMapping("/{staffId}/followup/{enquiryId}")
    public ResponseEntity<?>
    addFollowup(
            @PathVariable Long staffId,
            @PathVariable Long enquiryId,
            @RequestBody TelecallingFollowupRequest request) {

        return ResponseEntity.ok(
                telecallingService.addFollowup(
                        staffId,
                        enquiryId,
                        request));
    }

    @GetMapping("/{staffId}/history/{enquiryId}")
    public ResponseEntity<List<TelecallingFollowup>>
    getFollowupHistory(
            @PathVariable Long staffId,
            @PathVariable Long enquiryId) {

        return ResponseEntity.ok(
                telecallingService.getFollowupHistory(
                        staffId,
                        enquiryId));
    }

    /*
     =====================================
     ENQUIRY -> STUDENT
     =====================================
     */

    @PostMapping("/{staffId}/update/{enquiryId}")
    public ResponseEntity<?> updateEnquiryStatus(
            @PathVariable EnquiryStatus status,
            @PathVariable Long enquiryId) {

        return ResponseEntity.ok(
                telecallingService.updateEnquiryStatus(
                        status,
                        enquiryId));
    }
}