package com.example.MpApp.repository.teamlead;

import com.example.MpApp.entity.teamlead.TeamLeadPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamLeadPermissionRepository extends JpaRepository<TeamLeadPermission, Long> {

    /**
     * Strictly counts already APPROVED monthly permissions for a specific Team Lead.
     * Uses EXTRACT to ensure clean SQL translation for PostgreSQL.
     */
    @Query("SELECT COUNT(p) FROM TeamLeadPermission p WHERE p.teamLead.id = :teamLeadId " +
            "AND EXTRACT(MONTH FROM p.permissionDate) = :month " +
            "AND EXTRACT(YEAR FROM p.permissionDate) = :year " +
            "AND p.status = 'APPROVED'")
    long countApprovedPermissionsByLeadAndMonth(
            @Param("teamLeadId") Long teamLeadId,
            @Param("month") int month,
            @Param("year") int year);

    List<TeamLeadPermission> findByStatusIgnoreCase(String status);
}