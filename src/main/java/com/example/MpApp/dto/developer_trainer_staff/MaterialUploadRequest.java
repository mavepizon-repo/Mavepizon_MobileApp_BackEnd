package com.example.MpApp.dto.developer_trainer_staff;

import lombok.Data;

@Data
public class MaterialUploadRequest {

    /*
    =========================================
    COURSE
    =========================================
    */

    private Long courseId;


    /*
    =========================================
    MATERIAL TITLE
    =========================================
    */

    private String title;
}