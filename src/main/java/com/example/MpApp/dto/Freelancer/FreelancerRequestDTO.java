package com.example.MpApp.dto.Freelancer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class FreelancerRequestDTO {
    @NotBlank(message = "Name is required")
    private String name;
    @PositiveOrZero(message = "Year of passing must be zero or positive")
    private Integer yearOfPassing;
    @PositiveOrZero(message = "Experience must be zero or positive")
    private Double experience;
    private String district;
    private String address;
    @Pattern(regexp = "^\\d{10}$", message = "Mobile number must be exactly 10 digits")
    private String mobileNo;
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    private String resume;
    private String aadhaar;
    private List<String> techStackNames; // e.g. ["Java", "Spring Boot"]
    // getters, setters
}