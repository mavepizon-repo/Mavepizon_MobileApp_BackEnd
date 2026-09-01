package com.example.MpApp.repository.telecallerstaff;

import com.example.MpApp.entity.telecallerstaff.TelecallingCallLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TelecallingCallLogRepository extends JpaRepository<TelecallingCallLog, Long> {

    List<TelecallingCallLog> findByEnquiryIdOrderByCallTimeDesc(Long enquiryId);

    @Query("""
        SELECT c FROM TelecallingCallLog c
        WHERE c.callStatus = 'COMPLETED'
        AND (:date IS NULL OR CAST(c.callTime AS date) = :date)
        AND (:staffId IS NULL OR c.staff.id = :staffId)
        ORDER BY c.callTime DESC
    """)
    List<TelecallingCallLog> findAdminCompletedCalls(
            @Param("date") LocalDate date,
            @Param("staffId") Long staffId
    );
}