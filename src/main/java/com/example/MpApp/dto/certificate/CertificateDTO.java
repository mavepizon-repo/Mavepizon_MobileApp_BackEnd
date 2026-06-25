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
}