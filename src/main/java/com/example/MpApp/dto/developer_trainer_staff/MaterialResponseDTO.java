package com.example.MpApp.dto.developer_trainer_staff;

import lombok.Data;

@Data
public class MaterialResponseDTO {

    /*
    =========================================
    MATERIAL ID
    =========================================
    */

    private Long id;


    /*
    =========================================
    COURSE DETAILS
    =========================================
    */

    private Long courseId;

    private String courseName;


    /*
    =========================================
    MATERIAL DETAILS
    =========================================
    */

    private String title;

    private String fileUrl;
}