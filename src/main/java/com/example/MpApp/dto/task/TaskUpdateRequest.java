package com.example.MpApp.dto.task;

import com.example.MpApp.entity.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskUpdateRequest {

    private Integer progressPercentage;
    private String workDoneToday;
    private String blockers;
    private String comments;
    private String attachmentUrl;
    private TaskStatus status;



}