package com.example.MpApp.dto.officestaff;

import lombok.Data;

@Data
public class CreatedByDTO {

    private Long id;

    private String name;

    private String email;

    private String branch;

    private String teamLeadId;

    private String type;
}