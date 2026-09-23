package com.example.MpApp.dto.Freelancer;

import com.example.MpApp.entity.freelancer.TaskStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskSubmissionRequestDTO {
    @NotNull(message = "Freelancer task ID is required")
    @Positive(message = "Freelancer task ID must be positive")
    private Long freelancerTaskId;
    @NotNull(message = "Status is required")
    private TaskStatus status;
    private String feedback;
    // getters, setters
}