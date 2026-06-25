package com.example.MpApp.dto.developer_trainer_staff;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchStudentDTO {
    private Long enrollmentId;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String studentCode;
    private String department;
    private String collegeName;
    private LocalDate enrolledDate;
}