package com.example.MpApp.repository.officestaff;

import com.example.MpApp.entity.officestaff.OfficeStaffPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OfficeStaffPermissionRepository extends JpaRepository<OfficeStaffPermission, Long> {

    /**
     * Counts active or approved permissions for a specific staff member during a given month and year.
     * Fixed for PostgreSQL compatibility using standard JPQL temporal functions.
     */
    @Query("SELECT COUNT(p) FROM OfficeStaffPermission p WHERE p.staff.id = :staffId " +
            "AND MONTH(p.permissionDate) = :month " +
            "AND YEAR(p.permissionDate) = :year " +
            "AND p.status = 'APPROVED'") // Fix: Strictly count approved limits
    long countPermissionsByStaffAndMonth(@Param("staffId") Long staffId,
                                         @Param("month") int month,
                                         @Param("year") int year);

    /**
     * Retrieves all permission records for a specific staff member ordered by date descending.
     */
    List<OfficeStaffPermission> findByStaffIdOrderByPermissionDateDesc(Long staffId);

    /**
     * Fetches all branch permissions.
     * Uses TRIM() and UPPER() to make string matching immune to database whitespace and case issues.
     */
    @Query("SELECT p FROM OfficeStaffPermission p " +
            "WHERE TRIM(UPPER(p.staff.branch)) = TRIM(UPPER(:branch)) " +
            "ORDER BY p.permissionDate DESC")
    List<OfficeStaffPermission> findAllByBranch(@Param("branch") String branch);

    /**
     * Fetches filtering records matching a specific operational status within a branch hierarchy.
     * Uses TRIM() and UPPER() to prevent trailing/leading whitespace or capitalization bugs from hiding rows.
     */
    @Query("SELECT p FROM OfficeStaffPermission p " +
            "WHERE TRIM(UPPER(p.staff.branch)) = TRIM(UPPER(:branch)) " +
            "AND p.status = :status " +
            "ORDER BY p.permissionDate DESC")
    List<OfficeStaffPermission> findByBranchAndStatus(@Param("branch") String branch, @Param("status") String status);

    Optional<OfficeStaffPermission> findByStaffIdAndPermissionDateAndStatus(
            Long staffId, LocalDate permissionDate, String status);

    void deleteByStaffId(Long staffId);
}