package com.example.MpApp.repository.developer_trainer;

import com.example.MpApp.dto.developer_trainer_staff.BatchStudentDTO;
import com.example.MpApp.entity.developer_trainer_staff.BatchStudents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchStudentRepository extends JpaRepository<BatchStudents, Long> {

    List<BatchStudents> findByBatchId(Long batchId);

    Long countByBatchId(Long batchId);

    boolean existsByBatchIdAndStudentId(Long batchId, Long studentId);

    /*
     ==================================
     OPTIMIZED JOIN FETCH QUERY
     ==================================
     */
    @Query("""
    SELECT new com.example.MpApp.dto.developer_trainer_staff.BatchStudentDTO(
        bs.id, 
        s.id, 
        s.name, 
        s.email, 
        s.studentId, 
        s.department, 
        s.collegeName, 
        bs.enrolledDate
    )
    FROM BatchStudents bs 
    JOIN bs.student s 
    WHERE bs.batch.id = :batchId
    """)
    List<BatchStudentDTO> findBatchStudentsFlat(@Param("batchId") Long batchId);
}