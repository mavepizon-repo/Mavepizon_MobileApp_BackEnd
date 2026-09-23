package com.example.MpApp.dto.Freelancer;

import com.example.MpApp.entity.freelancer.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FreelancerTaskRequestDTO {

    @NotBlank(message = "Organization name is required")
    private String orgName;

    @NotNull(message = "Number of days is required")
    @Positive(message = "Number of days must be positive")
    private Integer noOfDays;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private String meetingLink;

    private String meetingEmail;

    private String meetingPassword;

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Domain is required")
    private String domain;

    @Positive(message = "Number of students must be positive")
    private Integer noOfStudents;

    private TaskStatus status;

    @NotEmpty(message = "At least one freelancer is required")
    private List<Long> freelancerIds;
}