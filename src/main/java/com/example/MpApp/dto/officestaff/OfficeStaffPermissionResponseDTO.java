package com.example.MpApp.dto.officestaff;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
public class OfficeStaffPermissionResponseDTO {
    private Long id;

    private Long staffId;

    private String staffName;

    private String staffBranch;

    private LocalDate permissionDate;

    private int durationHours;

    private String reason;

    private String status;

    private LocalDateTime createdAt;
}
