package com.example.MpApp.repository.developer_trainer;

import com.example.MpApp.entity.developer_trainer_staff.CashFeeConfirmation;
import com.example.MpApp.entity.developer_trainer_staff.CourseMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseMaterialRepository
        extends JpaRepository<CourseMaterial,Long> {

    List<CourseMaterial>
    findByBatchId(Long batchId);
}
