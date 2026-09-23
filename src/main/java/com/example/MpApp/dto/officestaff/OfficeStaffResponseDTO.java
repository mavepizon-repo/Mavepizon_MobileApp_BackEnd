package com.example.MpApp.dto.officestaff;

import java.time.LocalDate;
import lombok.Data;

@Data
public class OfficeStaffResponseDTO {

    private Long id;

    private String staffId;
    private String employeeId;

    private String name;
    private String email;
    private String mobileNumber;

    private String gender;
    private String bloodGroup;

    private String branch;
    private String branchName;

    private String category;
    private String role;

    private String degree;
    private Integer yearPassedOut;

    private LocalDate joiningDate;
    private String nativePlace;

    private Integer experience;
    private String previousCompany;
    private String skills;

    private String aadhaarFile;
    private String profilePhoto;
    private String resumeFile;
    private String experienceCertificate;

    private Integer score;

    private boolean active;
    private String approvalStatus;

    private CreatedByDTO createdBy;
}