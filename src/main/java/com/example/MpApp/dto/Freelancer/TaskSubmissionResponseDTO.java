package com.example.MpApp.dto.Freelancer;

import com.example.MpApp.entity.freelancer.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskSubmissionResponseDTO {
    private Long id;
    private Long freelancerTaskId;
    private TaskStatus status;
    private String notes;
    private String feedback;
    // getters, setters
}