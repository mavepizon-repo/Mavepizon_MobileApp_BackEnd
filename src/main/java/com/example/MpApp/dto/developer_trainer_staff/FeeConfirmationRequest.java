package com.example.MpApp.dto.developer_trainer_staff;
import lombok.Data;

@Data
public class FeeConfirmationRequest {
    private Long studentId;
    private Long batchId;
    private Double amount;
    private String remarks;
}