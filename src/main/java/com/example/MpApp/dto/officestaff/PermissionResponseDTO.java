package com.example.MpApp.dto.officestaff;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter

public class PermissionResponseDTO {


        private Long id;

        private LocalDate permissionDate;

        private int durationHours;

        private String reason;

        private String status;

        private LocalDateTime createdAt;

        // Staff details
        private String staffName;

        private String branch;
}
