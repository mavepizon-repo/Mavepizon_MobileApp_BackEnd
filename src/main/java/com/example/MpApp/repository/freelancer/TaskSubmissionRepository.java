package com.example.MpApp.repository.freelancer;


import com.example.MpApp.entity.freelancer.TaskSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskSubmissionRepository extends JpaRepository<TaskSubmission, Long> {
    Optional<TaskSubmission> findByFreelancerTaskId(Long freelancerTaskId);
}