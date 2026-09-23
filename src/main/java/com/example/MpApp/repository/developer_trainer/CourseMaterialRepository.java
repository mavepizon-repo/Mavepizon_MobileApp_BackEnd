package com.example.MpApp.repository.developer_trainer;

import com.example.MpApp.entity.developer_trainer_staff.CourseMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseMaterialRepository
        extends JpaRepository<CourseMaterial, Long> {

    // =========================================================
    // GET MATERIALS BY COURSE
    // =========================================================

    List<CourseMaterial> findByCourseId(Long courseId);


    // =========================================================
    // GET MATERIALS UPLOADED BY STAFF
    // =========================================================

    List<CourseMaterial> findByUploadedByStaffId(
            Long uploadedByStaffId
    );


    // =========================================================
    // GET MATERIALS BY COURSE AND STAFF
    // =========================================================

    List<CourseMaterial> findByCourseIdAndUploadedByStaffId(
            Long courseId,
            Long uploadedByStaffId
    );
}