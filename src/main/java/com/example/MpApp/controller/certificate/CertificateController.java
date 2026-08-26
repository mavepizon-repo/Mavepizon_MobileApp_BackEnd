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

    @Autowired
    private CertificateService service;


    // =========================================================
    // INITIATE COURSE CERTIFICATES
    // =========================================================

    @PostMapping("/initiate/course/{courseId}")
    public ResponseEntity<?> initiateCourseCertificates(
            @PathVariable Long courseId) {

        try {

            return ResponseEntity.ok(
                    service.initiateCourseCertificates(
                            courseId
                    )
            );

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }


    // =========================================================
    // GET CERTIFICATES BY COURSE
    // =========================================================

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<CertificateDTO>>
    getCertificatesByCourse(
            @PathVariable Long courseId) {

        return ResponseEntity.ok(
                service.getCertificatesByCourse(
                        courseId
                )
        );
    }


    // =========================================================
    // GET PENDING CERTIFICATES
    // =========================================================

    @GetMapping("/pending")
    public ResponseEntity<List<CertificateDTO>>
    getPendingCertificates() {

        return ResponseEntity.ok(
                service.getPendingCertificates()
        );
    }


    // =========================================================
    // GET STUDENT CERTIFICATES
    // =========================================================

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<CertificateDTO>>
    getStudentCertificates(
            @PathVariable Long studentId) {

        return ResponseEntity.ok(
                service.getStudentCertificates(
                        studentId
                )
        );
    }


    // =========================================================
    // UPLOAD CERTIFICATE FILE
    // =========================================================

    @PostMapping("/{id}/upload")
    public ResponseEntity<?> uploadCertificate(
            @PathVariable Long id,
            @RequestParam("file")
            MultipartFile file) {

        try {

            return ResponseEntity.ok(
                    service.uploadCertificateFile(
                            id,
                            file
                    )
            );

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }


    // =========================================================
    // UPDATE CERTIFICATE STATUS
    // =========================================================

    @PutMapping("/status/{id}")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        try {

            return ResponseEntity.ok(
                    service.updateCertificateStatus(
                            id,
                            status
                    )
            );

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }


    // =========================================================
    // GET ALL CERTIFICATES
    // =========================================================

    @GetMapping("/all")
    public ResponseEntity<List<CertificateDTO>>
    getAllCertificates() {

        return ResponseEntity.ok(
                service.getAllCertificates()
        );
    }


    // =========================================================
    // DELETE CERTIFICATE
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCertificate(
            @PathVariable Long id) {

        try {

            service.deleteCertificate(id);

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Certificate deleted successfully"
                    )
            );

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }
}