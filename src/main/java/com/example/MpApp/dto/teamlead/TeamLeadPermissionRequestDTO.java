package com.example.MpApp.dto.teamlead;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TeamLeadPermissionRequestDTO {
    private LocalDate permissionDate;
    private Integer durationHours;
    private String reason;
}