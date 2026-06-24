package com.example.MpApp.controller.developer_trainer;

import com.example.MpApp.dto.developer_trainer_staff.*;
import com.example.MpApp.entity.developer_trainer_staff.*;
import com.example.MpApp.service.developer_trainer.DeveloperTrainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trainer")
@RequiredArgsConstructor
public class DeveloperTrainerController {

    private final DeveloperTrainerService trainerService;

    /*
     =====================================
     BATCH MANAGEMENT
     =====================================
     */
    @GetMapping("/{staffId}/batches")
    public ResponseEntity<?> getAssignedBatches(@PathVariable Long staffId) {
        try {
            return ResponseEntity.ok(trainerService.getAssignedBatches(staffId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{staffId}/online-batches")
    public ResponseEntity<?> getOnlineBatches(@PathVariable Long staffId) {
        try {
            return ResponseEntity.ok(trainerService.getOnlineBatches(staffId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{staffId}/offline-batches")
    public ResponseEntity<?> getOfflineBatches(@PathVariable Long staffId) {
        try {
            return ResponseEntity.ok(trainerService.getOfflineBatches(staffId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /*
     =====================================
     ATTENDANCE & MATERIALS
     =====================================
     */
    @PostMapping("/{staffId}/attendance")
    public ResponseEntity<?> markAttendance(@PathVariable Long staffId, @RequestBody AttendanceRequest request) {
        try {
            return ResponseEntity.ok(trainerService.markAttendance(staffId, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "/{staffId}/material", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadMaterial(
            @PathVariable Long staffId,
            @RequestParam("batchId") Long batchId,
            @RequestParam("title") String title,
            @RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(trainerService.uploadMaterial(staffId, batchId, title, file));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /*
     =====================================
     BATCH SETTINGS & FEES
     =====================================
     */
    @PutMapping("/{staffId}/zoom-link")
    public ResponseEntity<?> updateZoomLink(@PathVariable Long staffId, @RequestBody ZoomLinkRequest request) {
        try {
            return ResponseEntity.ok(trainerService.updateZoomLink(staffId, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{staffId}/fee-confirmation")
    public ResponseEntity<?> confirmFees(@PathVariable Long staffId, @RequestBody FeeConfirmationRequest request) {
        try {
            return ResponseEntity.ok(trainerService.confirmFees(staffId, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{staffId}/certificate-status")
    public ResponseEntity<?> requestCertificates(@PathVariable Long staffId, @RequestParam("batchId") Long batchId) {
        try {
            return ResponseEntity.ok(trainerService.updateCertificateStatus(staffId, batchId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{staffId}/dashboard")
    public ResponseEntity<?> dashboard(@PathVariable Long staffId) {
        try {
            return ResponseEntity.ok(trainerService.getDashboard(staffId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}