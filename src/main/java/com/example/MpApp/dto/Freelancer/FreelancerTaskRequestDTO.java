package com.example.MpApp.dto.Freelancer;

import com.example.MpApp.entity.freelancer.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FreelancerTaskRequestDTO {
    private String orgName;
    private Integer noOfDays;
    private LocalDate startDate;
    private LocalDate endDate;
    private String meetingLink;
    private String meetingEmail;
    private String meetingPassword;
    private String department;
    private String domain;
    private Integer noOfStudents;
    private String syllabus;
    private TaskStatus status;
    private List<Long> freelancerIds; // freelancers to assign
    // getters, setters
}