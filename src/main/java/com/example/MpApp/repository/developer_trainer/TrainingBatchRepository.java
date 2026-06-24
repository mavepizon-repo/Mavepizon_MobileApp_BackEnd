package com.example.MpApp.repository.developer_trainer;

import com.example.MpApp.dto.developer_trainer_staff.BatchDTO;
import com.example.MpApp.entity.developer_trainer_staff.TrainingBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingBatchRepository extends JpaRepository<TrainingBatch, Long> {

    @Query("SELECT new com.example.MpApp.dto.developer_trainer_staff.BatchDTO(" +
            "b.id, b.batchName, b.batchMode, b.zoomLink, oc.courseName, t.name) " +
            "FROM TrainingBatch b " +
            "LEFT JOIN b.offeredCourse oc " +  // 👈 Changed to LEFT JOIN
            "JOIN b.trainer t " +
            "WHERE t.id = :trainerId")
    List<BatchDTO> findByTrainerIdWithDetails(@Param("trainerId") Long trainerId);
}