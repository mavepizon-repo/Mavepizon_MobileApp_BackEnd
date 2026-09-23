package com.example.MpApp.dto.task;

import com.example.MpApp.entity.enums.Priority;
import com.example.MpApp.entity.enums.TaskType;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;


@Data
public class TaskRequestSome {

    private List<Long> staffIds;

    private Long teamLeadId;

    private String title;

    private String description;

    private LocalDate deadline;

    private TaskType taskType;

    private Priority priority;

    private Integer estimatedHours;

    private String remarks;
}
