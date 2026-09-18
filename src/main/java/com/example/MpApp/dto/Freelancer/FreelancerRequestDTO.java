package com.example.MpApp.dto.Freelancer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class FreelancerRequestDTO {
    private String name;
    private Integer yearOfPassing;
    private Double experience;
    private String district;
    private String address;
    private String mobileNo;
    private String email;
    private String password;
    private String resume;
    private String aadhaar;
    private List<String> techStackNames; // e.g. ["Java", "Spring Boot"]
    // getters, setters
}