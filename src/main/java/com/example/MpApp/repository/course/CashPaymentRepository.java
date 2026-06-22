package com.example.MpApp.repository.course;

import com.example.MpApp.entity.course.CashPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CashPaymentRepository extends JpaRepository<CashPayment, Long> {

    @Query("""
            SELECT cp
            FROM CashPayment cp
            JOIN FETCH cp.student s
            LEFT JOIN FETCH cp.registration r
            LEFT JOIN FETCH r.offeredCourse c
            WHERE s.studentId = :studentId
            """)
    List<CashPayment> findPaymentsByStudentId(@Param("studentId") String studentId);

    @Query("""
            SELECT cp
            FROM CashPayment cp
            JOIN FETCH cp.student s
            LEFT JOIN FETCH cp.registration r
            LEFT JOIN FETCH r.offeredCourse c
            """)
    List<CashPayment> findAllWithDetails();

    @Query("""
            SELECT cp
            FROM CashPayment cp
            JOIN FETCH cp.student s
            LEFT JOIN FETCH cp.registration r
            LEFT JOIN FETCH r.offeredCourse c
            WHERE cp.id = :id
            """)
    CashPayment findPaymentDetailsById(@Param("id") Long id);

    @Query("""
            SELECT cp
            FROM CashPayment cp
            JOIN FETCH cp.student s
            LEFT JOIN FETCH cp.registration r
            LEFT JOIN FETCH r.offeredCourse c
            WHERE cp.status = :status
            """)
    List<CashPayment> findByStatusWithDetails(@Param("status") String status);

    @Query("""
            SELECT cp
            FROM CashPayment cp
            JOIN FETCH cp.student s
            LEFT JOIN FETCH cp.registration r
            LEFT JOIN FETCH r.offeredCourse c
            WHERE cp.selectedStaffId = :staffId
            """)
    List<CashPayment> findBySelectedStaffIdWithDetails(@Param("staffId") Long staffId);
}