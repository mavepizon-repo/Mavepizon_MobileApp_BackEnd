package com.example.MpApp.dto.Freelancer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FreelancerResponseDTO {

    private Long id;

    private String name;

    private Integer yearOfPassing;

    private Double experience;

    private String district;

    private String address;

    private String mobileNo;

    private String email;

    // Cloudinary URL
    private String profile;

    // Cloudinary URL
    private String resume;

    // Cloudinary URL
    private String aadhaar;

    private List<String> techStackNames;
}