package com.example.MpApp.repository.developer_trainer;

import com.example.MpApp.entity.developer_trainer_staff.StudentAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StudentAttendanceRepository
        extends JpaRepository<StudentAttendance,Long> {

    Long countByBatchIdAndAttendanceDateAndPresent(
            Long batchId,
            LocalDate attendanceDate,
            Boolean present);

    List<StudentAttendance>
    findByBatchId(Long batchId);

    Long countByAttendanceDate(LocalDate attendanceDate);
}
