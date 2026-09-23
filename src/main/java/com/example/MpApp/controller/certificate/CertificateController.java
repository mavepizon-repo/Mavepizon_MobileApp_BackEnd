package com.example.MpApp.controller.certificate;

import com.example.MpApp.dto.certificate.CertificateDTO;
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
    @Autowired private CertificateService service;

    @PostMapping("/create/{registrationId}")
    public ResponseEntity<?> createCertificate(@PathVariable Long registrationId) {
        return ResponseEntity.ok(service.createCourseCertificate(registrationId));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<CertificateDTO>> getCertificatesByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(service.getCertificatesByCourse(courseId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<CertificateDTO>> getPendingCertificates() {
        return ResponseEntity.ok(service.getPendingCertificates());
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<CertificateDTO>> getStudentCertificates(@PathVariable Long studentId, @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(service.getStudentCertificates(studentId, authHeader));
    }

    @PostMapping("/{id}/upload")
    public ResponseEntity<?> uploadCertificate(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.uploadCertificateFile(id, file));
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(service.updateCertificateStatus(id, status));
    }

    @GetMapping("/all")
    public ResponseEntity<List<CertificateDTO>> getAllCertificates() {
        return ResponseEntity.ok(service.getAllCertificates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CertificateDTO> getCertificateById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getCertificateById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCertificate(@PathVariable Long id) {
        service.deleteCertificate(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Certificate deleted successfully"));
    }
}
