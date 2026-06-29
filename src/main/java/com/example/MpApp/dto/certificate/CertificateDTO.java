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
    private String batchName;
    private String status;
    private String fileUrl;
    private LocalDate issueDate;
    private String collegeName;
    private String department;

    // Add this to your CertificateDTO class
    public CertificateDTO(Long certificateId, Long studentId, String studentName, String studentCode,
                          String recordType, String batchName, String status, String fileUrl, LocalDate issueDate) {
        this.certificateId = certificateId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentCode = studentCode;
        this.recordType = recordType;
        this.batchName = batchName;
        this.status = status;
        this.fileUrl = fileUrl;
        this.issueDate = issueDate;
        // collegeName and department will remain null
    }
}