package com.example.MpApp.dto.telecallerstaff;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class TelecallingEnquiryRequest {

    private String studentName;

    private String phone;

    private String email;

    private String collegeName;

    private String department;

    private String city;

    private String address;

    private String district;

    private String interestedCourse;

    private LocalDate latestFollowupDate;

    private String remarks;

}