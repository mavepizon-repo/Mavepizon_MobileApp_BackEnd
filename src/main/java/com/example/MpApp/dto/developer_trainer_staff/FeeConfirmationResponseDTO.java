package com.example.MpApp.dto.developer_trainer_staff;

import lombok.Data;

@Data
public class FeeConfirmationResponseDTO {
    private Long id;
    private Long studentId;
    private Double amount;
    private String status;
}