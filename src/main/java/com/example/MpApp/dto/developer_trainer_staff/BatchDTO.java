package com.example.MpApp.dto.developer_trainer_staff;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor // Important: This allows the JPQL constructor query to work
public class BatchDTO {
    private Long batchId;
    private String batchName;
    private String batchMode;
    private String zoomLink;
    private String courseName;
    private String trainerName;
}