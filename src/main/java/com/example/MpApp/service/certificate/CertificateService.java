package com.example.MpApp.service.certificate;

import com.example.MpApp.dto.certificate.CertificateDTO;
import com.example.MpApp.entity.certificate.Certificate;
import com.example.MpApp.entity.course.StudentCourseRegistration;

import com.example.MpApp.repository.certificate.CertificateRepository;
import com.example.MpApp.repository.course.StudentCourseRegistrationRepository;

import com.example.MpApp.service.CloudinaryService;

import org.springframework.beans.factory.annotation.Autowired;
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
    private CloudinaryService cloudinaryService;


    // =========================================================
    // 1. CREATE CERTIFICATE RECORD
    // =========================================================
    // This does NOT generate the certificate file.
    //
    // It only creates a Certificate database record
    // for one eligible course registration.
    //
    // Frontend will design/generate the certificate PDF/image
    // and upload it later.
    // =========================================================

    @Transactional
    public CertificateDTO createCourseCertificate(
            Long registrationId) {

        // -----------------------------------------------------
        // FIND COURSE REGISTRATION
        // -----------------------------------------------------

        StudentCourseRegistration registration =
                courseRegistrationRepository
                        .findById(registrationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course registration not found for ID: "
                                                + registrationId
                                )
                        );


        // -----------------------------------------------------
        // CHECK PAYMENT STATUS
        // -----------------------------------------------------

        String paymentStatus = registration.getPaymentStatus();

        if (!("PAID".equalsIgnoreCase(paymentStatus)
                || "SUCCESS".equalsIgnoreCase(paymentStatus)
                || "COMPLETED".equalsIgnoreCase(paymentStatus))) {

            throw new RuntimeException(
                    "Certificate cannot be created because payment is not completed"
            );
        }


        // -----------------------------------------------------
        // CHECK DUPLICATE CERTIFICATE
        // -----------------------------------------------------

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


        // -----------------------------------------------------
        // CREATE CERTIFICATE
        // -----------------------------------------------------

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


        // -----------------------------------------------------
        // SAVE CERTIFICATE
        // -----------------------------------------------------

        Certificate savedCertificate =
                certificateRepository.save(
                        certificate
                );


        // -----------------------------------------------------
        // RETURN CREATED CERTIFICATE
        // -----------------------------------------------------

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
    //
    // Frontend generates the certificate PDF/image.
    //
    // Backend:
    // 1. Receives file
    // 2. Uploads to Cloudinary
    // 3. Saves Cloudinary URL
    // 4. Changes status to ISSUED
    // 5. Sets issue date
    // 6. Changes course registration certificateStatus
    //    to GENERATED
    //
    // =========================================================

    @Transactional
    public Map<String, String>
    uploadCertificateFile(
            Long certificateId,
            MultipartFile file) {


        // -----------------------------------------------------
        // FIND CERTIFICATE
        // -----------------------------------------------------

        Certificate certificate =
                certificateRepository
                        .findById(certificateId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Certificate record not found for ID: "
                                                + certificateId
                                )
                        );


        // -----------------------------------------------------
        // VALIDATE FILE
        // -----------------------------------------------------

        if (file == null ||
                file.isEmpty()) {

            throw new RuntimeException(
                    "Certificate file is empty or missing"
            );
        }


        // -----------------------------------------------------
        // UPLOAD FILE TO CLOUDINARY
        // -----------------------------------------------------

        String fileUrl =
                cloudinaryService.uploadFile(
                        file,
                        "certificates"
                );


        // -----------------------------------------------------
        // UPDATE CERTIFICATE
        // -----------------------------------------------------

        certificate.setFileUrl(
                fileUrl
        );


        certificate.setStatus(
                "ISSUED"
        );


        certificate.setIssueDate(
                LocalDate.now()
        );


        // -----------------------------------------------------
        // UPDATE COURSE REGISTRATION
        // -----------------------------------------------------

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


        // -----------------------------------------------------
        // SAVE CERTIFICATE
        // -----------------------------------------------------

        certificateRepository.save(
                certificate
        );


        // -----------------------------------------------------
        // RETURN RESPONSE
        // -----------------------------------------------------

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

    public List<CertificateDTO>
    getStudentCertificates(
            Long studentId) {

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


        // -----------------------------------------------------
        // FIND CERTIFICATE
        // -----------------------------------------------------

        Certificate certificate =
                certificateRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Certificate record not found for ID: "
                                                + id
                                )
                        );


        // -----------------------------------------------------
        // UPDATE STATUS
        // -----------------------------------------------------

        certificate.setStatus(
                status
        );


        // -----------------------------------------------------
        // SAVE
        // -----------------------------------------------------

        certificateRepository.save(
                certificate
        );


        // -----------------------------------------------------
        // RETURN RESPONSE
        // -----------------------------------------------------

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


        // -----------------------------------------------------
        // CHECK CERTIFICATE
        // -----------------------------------------------------

        if (!certificateRepository
                .existsById(id)) {

            throw new RuntimeException(
                    "Certificate record not found for ID: "
                            + id
            );
        }


        // -----------------------------------------------------
        // DELETE
        // -----------------------------------------------------

        certificateRepository.deleteById(
                id
        );
    }
}