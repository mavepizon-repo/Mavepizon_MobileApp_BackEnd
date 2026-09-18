package com.example.MpApp.dto.certificate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CertificateDTO {

    private Long certificateId;

    private Long studentId;

    private String studentName;

    private String studentCode;

    private String recordType;

    private String courseName;

    private String status;

    private String fileUrl;

    private LocalDate issueDate;

    private String collegeName;

    private String department;


    // =========================================================
    // CONSTRUCTOR WITHOUT COLLEGE / DEPARTMENT
    // =========================================================

    public CertificateDTO(
            Long certificateId,
            Long studentId,
            String studentName,
            String studentCode,
            String recordType,
            String courseName,
            String status,
            String fileUrl,
            LocalDate issueDate) {

        this.certificateId = certificateId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentCode = studentCode;
        this.recordType = recordType;
        this.courseName = courseName;
        this.status = status;
        this.fileUrl = fileUrl;
        this.issueDate = issueDate;
    }
}