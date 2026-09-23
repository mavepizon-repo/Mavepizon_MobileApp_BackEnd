package com.example.MpApp.dto.officestaff;

import lombok.Data;

@Data
public class StaffResponseDTO {

    private Long id;
    private String name;
    private String email;
    private String role;
    private String mobileNumber;
    private String branch;
    private String ApprovalStatus;


}
