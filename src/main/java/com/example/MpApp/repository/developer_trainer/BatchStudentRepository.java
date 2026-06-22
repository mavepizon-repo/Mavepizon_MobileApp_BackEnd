package com.example.MpApp.repository.developer_trainer;

import com.example.MpApp.entity.developer_trainer_staff.BatchStudents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchStudentRepository
        extends JpaRepository<BatchStudents,Long> {

    List<BatchStudents>
    findByBatchId(Long batchId);

    Long countByBatchId(Long batchId);
}