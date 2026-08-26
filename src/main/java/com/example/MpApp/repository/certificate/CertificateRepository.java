package com.example.MpApp.repository.certificate;

import com.example.MpApp.dto.certificate.CertificateDTO;
import com.example.MpApp.entity.certificate.Certificate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CertificateRepository
        extends JpaRepository<Certificate, Long> {


    // =========================================================
    // PENDING CERTIFICATES
    // =========================================================

    @Query("""
            SELECT new com.example.MpApp.dto.certificate.CertificateDTO(
                c.id,
                s.id,
                s.name,
                s.studentId,
                c.recordType,
                co.courseName,
                c.status,
                c.fileUrl,
                c.issueDate,
                s.collegeName,
                s.department
            )
            FROM Certificate c
            JOIN c.student s
            LEFT JOIN c.courseRegistration cr
            LEFT JOIN cr.course co
            WHERE UPPER(c.status) = UPPER(:status)
            """)
    List<CertificateDTO> findByStatusIgnoreCaseFlat(
            @Param("status") String status
    );


    // =========================================================
    // STUDENT CERTIFICATES
    // =========================================================

    @Query("""
            SELECT new com.example.MpApp.dto.certificate.CertificateDTO(
                c.id,
                s.id,
                s.name,
                s.studentId,
                c.recordType,
                co.courseName,
                c.status,
                c.fileUrl,
                c.issueDate
            )
            FROM Certificate c
            JOIN c.student s
            LEFT JOIN c.courseRegistration cr
            LEFT JOIN cr.course co
            WHERE s.id = :studentId
            """)
    List<CertificateDTO> findByStudentIdFlat(
            @Param("studentId") Long studentId
    );


    // =========================================================
    // COURSE CERTIFICATES
    // =========================================================

    @Query("""
            SELECT new com.example.MpApp.dto.certificate.CertificateDTO(
                c.id,
                s.id,
                s.name,
                s.studentId,
                c.recordType,
                co.courseName,
                c.status,
                c.fileUrl,
                c.issueDate
            )
            FROM Certificate c
            JOIN c.student s
            JOIN c.courseRegistration cr
            JOIN cr.course co
            WHERE co.id = :courseId
            """)
    List<CertificateDTO> findByCourseIdFlat(
            @Param("courseId") Long courseId
    );


    // =========================================================
    // ALL CERTIFICATES
    // =========================================================

    @Query("""
            SELECT new com.example.MpApp.dto.certificate.CertificateDTO(
                c.id,
                s.id,
                s.name,
                s.studentId,
                c.recordType,
                co.courseName,
                c.status,
                c.fileUrl,
                c.issueDate,
                s.collegeName,
                s.department
            )
            FROM Certificate c
            JOIN c.student s
            LEFT JOIN c.courseRegistration cr
            LEFT JOIN cr.course co
            """)
    List<CertificateDTO> findAllCertificatesFlat();


    // =========================================================
    // PREVENT DUPLICATE COURSE CERTIFICATE
    // =========================================================

    boolean existsByCourseRegistrationIdAndRecordType(
            Long courseRegistrationId,
            String recordType
    );
}