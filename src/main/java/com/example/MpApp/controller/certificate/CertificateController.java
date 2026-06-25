package com.example.MpApp.controller.certificate;

import com.example.MpApp.dto.certificate.CertificateDTO;
import com.example.MpApp.entity.certificate.Certificate;
import com.example.MpApp.service.certificate.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/certificates")
@CrossOrigin("*")
public class CertificateController {

    @Autowired
    private CertificateService service;

    /*
     =================================================
     TRAINER ENDPOINTS
     =================================================
     */

    // A Trainer hits this button when the batch is finished to generate PENDING certificates for everyone
    @PostMapping("/initiate/batch/{batchId}")
    public ResponseEntity<?> initiateBatchCertificates(@PathVariable Long batchId) {
        try {
            return ResponseEntity.ok(service.initiateBatchCertificates(batchId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/batch/{batchId}")
    public ResponseEntity<List<CertificateDTO>> getCertificatesByBatch(@PathVariable Long batchId) {
        return ResponseEntity.ok(service.getCertificatesByBatch(batchId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<CertificateDTO>> getPendingCertificates() {
        return ResponseEntity.ok(service.getPendingCertificates());
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<CertificateDTO>> getMyCertificates(@PathVariable Long studentId) {
        return ResponseEntity.ok(service.getStudentCertificates(studentId));
    }

    // The Design team uploads the finished PDF/Image here
    @PostMapping("/{id}/upload")
    public ResponseEntity<?> uploadCertificate(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(service.uploadCertificateFile(id, file));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /*
     =================================================
     STUDENT ENDPOINTS
     =================================================
     */


}