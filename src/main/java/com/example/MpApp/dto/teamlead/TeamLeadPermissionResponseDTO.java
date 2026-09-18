package com.example.MpApp.dto.teamlead;


import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TeamLeadPermissionResponseDTO {

    private Long id;

    private Long teamLeadId;

    private String teamLeadName;

    private String teamLeadBranch;

    private LocalDate permissionDate;

    private Integer durationHours;

    private String reason;

    private String status;

    private String remarks;

    private LocalDateTime createdAt;
}
