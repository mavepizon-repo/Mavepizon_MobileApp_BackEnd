package com.example.MpApp.repository.teamlead;

import com.example.MpApp.entity.teamlead.TeamLeadPermission;
import org.apache.poi.sl.draw.geom.GuideIf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeamLeadPermissionRepository
        extends JpaRepository<TeamLeadPermission, Long> {

    /*
     * Count APPROVED permissions for a Team Lead
     * within a specific calendar month.
     */
    long countByTeamLeadIdAndPermissionDateBetweenAndStatusIgnoreCase(
            Long teamLeadId,
            LocalDate startDate,
            LocalDate endDate,
            String status
    );

    /*
     * Get permissions by status.
     *
     * Example:
     * PENDING
     * APPROVED
     * REJECTED
     */
    List<TeamLeadPermission> findByStatusIgnoreCase(String status);

    /*
     * Get all permission requests of a Team Lead.
     */
    List<TeamLeadPermission> findByTeamLeadId(Long teamLeadId);

    Optional<TeamLeadPermission> findByTeamLeadIdAndPermissionDateAndStatus(Long teamLeadId, LocalDate permissionDate , String status);
}