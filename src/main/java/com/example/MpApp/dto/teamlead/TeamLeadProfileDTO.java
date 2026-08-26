package com.example.MpApp.dto.teamlead;

import lombok.Data;

@Data
public class TeamLeadProfileDTO {
    private Long id;
    private String name;
    private String teamLeadId;
    private String branch;
    private String email;
    private String mobileNumber;
    private String role;
    private String profilePhoto;
    private Integer performanceScore;
    private Boolean active;
}
