package com.example.MpApp.repository.task;

import com.example.MpApp.entity.enums.TaskStatus;
import com.example.MpApp.entity.task.TaskUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskUpdateRepository
        extends JpaRepository<TaskUpdate, Long> {

    List<TaskUpdate> findByTaskId(Long taskId);

    List<TaskUpdate> findByStatus(TaskStatus status);

    List<TaskUpdate> findByTaskIdOrderByUpdatedAtDesc(
            Long taskId
    );
}