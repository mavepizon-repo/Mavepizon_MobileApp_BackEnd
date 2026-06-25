package com.example.MpApp.repository.certificate;

import com.example.MpApp.dto.certificate.CertificateDTO;
import com.example.MpApp.entity.certificate.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    @Query("SELECT new com.example.MpApp.dto.certificate.CertificateDTO(" +
            "c.id, s.id, s.name, s.studentId, c.recordType, tb.batchName, c.status, c.fileUrl, c.issueDate) " +
            "FROM Certificate c " +
            "JOIN c.student s " +
            "LEFT JOIN c.trainingBatch tb " +
            "WHERE UPPER(c.status) = UPPER(:status)")
    List<CertificateDTO> findByStatusIgnoreCaseFlat(@Param("status") String status);

    @Query("SELECT new com.example.MpApp.dto.certificate.CertificateDTO(" +
            "c.id, s.id, s.name, s.studentId, c.recordType, tb.batchName, c.status, c.fileUrl, c.issueDate) " +
            "FROM Certificate c " +
            "JOIN c.student s " +
            "LEFT JOIN c.trainingBatch tb " +
            "WHERE s.id = :studentId")
    List<CertificateDTO> findByStudentIdFlat(@Param("studentId") Long studentId);

    @Query("SELECT new com.example.MpApp.dto.certificate.CertificateDTO(" +
            "c.id, s.id, s.name, s.studentId, c.recordType, tb.batchName, c.status, c.fileUrl, c.issueDate) " +
            "FROM Certificate c " +
            "JOIN c.student s " +
            "LEFT JOIN c.trainingBatch tb " +
            "WHERE tb.id = :batchId")
    List<CertificateDTO> findByTrainingBatchIdFlat(@Param("batchId") Long batchId);
}