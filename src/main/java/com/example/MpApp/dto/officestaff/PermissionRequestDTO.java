package com.example.MpApp.dto.officestaff;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class PermissionRequestDTO {
    private LocalDate permissionDate;
    private int durationHours; // Should accept 1 or 2
    private String reason;
}