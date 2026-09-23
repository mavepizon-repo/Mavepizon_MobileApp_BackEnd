package com.example.MpApp.service.certificate;

import com.example.MpApp.dto.certificate.CertificateDTO;
import com.example.MpApp.entity.certificate.Certificate;
import com.example.MpApp.entity.course.StudentCourseRegistration;
import com.example.MpApp.entity.student.Student;

import com.example.MpApp.exception.OwnershipViolationException;
import com.example.MpApp.exception.ResourceNotFoundException;

import com.example.MpApp.repository.certificate.CertificateRepository;
import com.example.MpApp.repository.course.StudentCourseRegistrationRepository;
import com.example.MpApp.repository.student.StudentRepository;

import com.example.MpApp.service.CloudinaryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class CertificateService {

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private StudentCourseRegistrationRepository
            courseRegistrationRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CloudinaryService cloudinaryService;


    // =========================================================
    // 1. CREATE CERTIFICATE RECORD
    // =========================================================

    @Transactional
    public CertificateDTO createCourseCertificate(
            Long registrationId) {

        StudentCourseRegistration registration =
                courseRegistrationRepository
                        .findById(registrationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course registration not found for ID: "
                                                + registrationId
                                )
                        );

        String paymentStatus = registration.getPaymentStatus();

        if (!("PAID".equalsIgnoreCase(paymentStatus)
                || "SUCCESS".equalsIgnoreCase(paymentStatus)
                || "COMPLETED".equalsIgnoreCase(paymentStatus))) {

            throw new RuntimeException(
                    "Certificate cannot be created because payment is not completed"
            );
        }

        boolean alreadyExists =
                certificateRepository
                        .existsByCourseRegistrationIdAndRecordType(
                                registrationId,
                                "COURSE"
                        );

        if (alreadyExists) {

            throw new RuntimeException(
                    "Certificate already exists for this registration"
            );
        }

        Certificate certificate =
                new Certificate();

        certificate.setStudent(
                registration.getStudent()
        );

        certificate.setCourseRegistration(
                registration
        );

        certificate.setRecordType(
                "COURSE"
        );

        certificate.setStatus(
                "PENDING"
        );

        Certificate savedCertificate =
                certificateRepository.save(
                        certificate
                );

        return certificateRepository
                .findByIdFlat(
                        savedCertificate.getId()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Certificate created but could not be retrieved"
                        )
                );
    }


    // =========================================================
    // 2. GET ALL CERTIFICATES
    // =========================================================

    public List<CertificateDTO>
    getAllCertificates() {

        return certificateRepository
                .findAllCertificatesFlat();
    }


    // =========================================================
    // 3. GET PARTICULAR CERTIFICATE BY CERTIFICATE ID
    // =========================================================

    public CertificateDTO
    getCertificateById(
            Long id) {

        return certificateRepository
                .findByIdFlat(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Certificate not found for ID: "
                                        + id
                        )
                );
    }


    // =========================================================
    // 4. UPLOAD CERTIFICATE FILE
    // =========================================================

    @Transactional
    public Map<String, String>
    uploadCertificateFile(
            Long certificateId,
            MultipartFile file) {

        Certificate certificate =
                certificateRepository
                        .findById(certificateId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Certificate record not found for ID: "
                                                + certificateId
                                )
                        );

        if (file == null ||
                file.isEmpty()) {

            throw new RuntimeException(
                    "Certificate file is empty or missing"
            );
        }

        String fileUrl =
                cloudinaryService.uploadFile(
                        file,
                        "certificates"
                );

        certificate.setFileUrl(
                fileUrl
        );

        certificate.setStatus(
                "ISSUED"
        );

        certificate.setIssueDate(
                LocalDate.now()
        );

        if (certificate.getCourseRegistration()
                != null) {

            StudentCourseRegistration registration =
                    certificate.getCourseRegistration();

            registration.setCertificateStatus(
                    "GENERATED"
            );

            courseRegistrationRepository.save(
                    registration
            );
        }

        certificateRepository.save(
                certificate
        );

        return Map.of(
                "message",
                "Certificate uploaded and marked as ISSUED",

                "fileUrl",
                fileUrl
        );
    }


    // =========================================================
    // 5. GET PENDING CERTIFICATES
    // =========================================================

    public List<CertificateDTO>
    getPendingCertificates() {

        return certificateRepository
                .findByStatusIgnoreCaseFlat(
                        "PENDING"
                );
    }


    // =========================================================
    // 6. GET COURSE CERTIFICATES
    // =========================================================

    public List<CertificateDTO>
    getCertificatesByCourse(
            Long courseId) {

        return certificateRepository
                .findByCourseIdFlat(
                        courseId
                );
    }


    // =========================================================
    // 7. GET STUDENT CERTIFICATES
    // =========================================================
    //
    // SECURITY FIX:
    // A STUDENT can only access their own certificates.
    //
    // ADMIN / TEAM_LEAD can still access certificates
    // for management purposes.
    // =========================================================

    public List<CertificateDTO>
    getStudentCertificates(
            Long studentId,
            String authHeader) {

        // -----------------------------------------------------
        // Validate Authorization header
        // -----------------------------------------------------

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            throw new OwnershipViolationException(
                    "Authentication required"
            );
        }

        // -----------------------------------------------------
        // Get authenticated user from JWT security context
        // -----------------------------------------------------

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new OwnershipViolationException(
                    "Authentication required"
            );
        }

        // -----------------------------------------------------
        // Check whether logged-in user is a student
        // -----------------------------------------------------

        boolean isStudent =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                "ROLE_STUDENT".equals(
                                        authority.getAuthority()
                                )
                        );

        // -----------------------------------------------------
        // Student ownership verification
        // -----------------------------------------------------

        if (isStudent) {

            String email =
                    authentication.getName();

            Student loggedInStudent =
                    studentRepository
                            .findByEmail(email)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Authenticated student not found"
                                    )
                            );

            // Requested student ID must belong to
            // the currently authenticated student.

            if (!loggedInStudent.getId().equals(studentId)) {

                throw new OwnershipViolationException(
                        "You are not allowed to access another student's certificates"
                );
            }
        }

        // -----------------------------------------------------
        // Ownership verified
        // -----------------------------------------------------

        return certificateRepository
                .findByStudentIdFlat(
                        studentId
                );
    }


    // =========================================================
    // 8. UPDATE CERTIFICATE STATUS
    // =========================================================

    @Transactional
    public Map<String, String>
    updateCertificateStatus(
            Long id,
            String status) {

        Certificate certificate =
                certificateRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Certificate record not found for ID: "
                                                + id
                                )
                        );

        certificate.setStatus(
                status
        );

        certificateRepository.save(
                certificate
        );

        return Map.of(
                "message",
                "Certificate status updated to "
                        + status
        );
    }


    // =========================================================
    // 9. DELETE CERTIFICATE
    // =========================================================

    @Transactional
    public void deleteCertificate(
            Long id) {

        if (!certificateRepository
                .existsById(id)) {

            throw new RuntimeException(
                    "Certificate record not found for ID: "
                            + id
            );
        }

        certificateRepository.deleteById(
                id
        );
    }
}