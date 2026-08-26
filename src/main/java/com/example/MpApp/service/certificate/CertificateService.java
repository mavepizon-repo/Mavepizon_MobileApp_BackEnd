package com.example.MpApp.service.certificate;

import com.example.MpApp.dto.certificate.CertificateDTO;
import com.example.MpApp.entity.certificate.Certificate;
import com.example.MpApp.entity.course.Course;
import com.example.MpApp.entity.course.StudentCourseRegistration;

import com.example.MpApp.repository.certificate.CertificateRepository;
import com.example.MpApp.repository.course.CourseRepository;
import com.example.MpApp.repository.course.StudentCourseRegistrationRepository;

import com.example.MpApp.service.CloudinaryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CertificateService {

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentCourseRegistrationRepository
            courseRegistrationRepository;

    @Autowired
    private CloudinaryService cloudinaryService;


    // =========================================================
    // 1. INITIATE CERTIFICATES BY COURSE
    // =========================================================

    @Transactional
    public Map<String, String>
    initiateCourseCertificates(Long courseId) {

        Course course =
                courseRepository.findById(courseId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course not found for ID: "
                                                + courseId
                                )
                        );


        // -----------------------------------------------------
        // GET REGISTERED STUDENTS
        // -----------------------------------------------------

        List<StudentCourseRegistration> registrations =
                courseRegistrationRepository
                        .findByCourseId(courseId);


        if (registrations.isEmpty()) {

            throw new RuntimeException(
                    "No student registrations found for this course"
            );
        }


        // -----------------------------------------------------
        // CREATE CERTIFICATES
        // -----------------------------------------------------

        List<Certificate> certificatesToSave =
                new ArrayList<>();


        for (StudentCourseRegistration registration
                : registrations) {


            /*
             * Don't create another certificate if
             * one already exists for this registration.
             */

            boolean alreadyExists =
                    certificateRepository
                            .existsByCourseRegistrationIdAndRecordType(
                                    registration.getId(),
                                    "COURSE"
                            );


            if (alreadyExists) {
                continue;
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


            certificatesToSave.add(
                    certificate
            );
        }


        // -----------------------------------------------------
        // SAVE
        // -----------------------------------------------------

        if (!certificatesToSave.isEmpty()) {

            certificateRepository.saveAll(
                    certificatesToSave
            );
        }


        return Map.of(
                "message",
                "Successfully queued "
                        + certificatesToSave.size()
                        + " PENDING certificates for course: "
                        + course.getCourseName()
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
    // 3. UPLOAD CERTIFICATE FILE
    // =========================================================

    @Transactional
    public Map<String, String>
    uploadCertificateFile(
            Long certificateId,
            MultipartFile file) {


        Certificate certificate =
                certificateRepository.findById(
                                certificateId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Certificate record not found"
                                )
                        );


        if (file == null ||
                file.isEmpty()) {

            throw new RuntimeException(
                    "Certificate file is empty or missing"
            );
        }


        // -----------------------------------------------------
        // UPLOAD
        // -----------------------------------------------------

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
    // 4. GET PENDING CERTIFICATES
    // =========================================================

    public List<CertificateDTO>
    getPendingCertificates() {

        return certificateRepository
                .findByStatusIgnoreCaseFlat(
                        "PENDING"
                );
    }


    // =========================================================
    // 5. GET COURSE CERTIFICATES
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
    // 6. GET STUDENT CERTIFICATES
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
    // 7. UPDATE CERTIFICATE STATUS
    // =========================================================

    @Transactional
    public Map<String, String>
    updateCertificateStatus(
            Long id,
            String status) {


        Certificate certificate =
                certificateRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Certificate record not found"
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
    // 8. DELETE CERTIFICATE
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