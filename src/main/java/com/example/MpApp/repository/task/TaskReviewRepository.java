package com.example.MpApp.repository.task;

import com.example.MpApp.entity.task.TaskReview;
import com.example.MpApp.entity.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskReviewRepository
        extends JpaRepository<TaskReview, Long> {

    List<TaskReview> findByTaskId(Long taskId);

    List<TaskReview> findByVerificationStatus(
            VerificationStatus verificationStatus
    );
}