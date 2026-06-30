package com.example.MpApp.repository.task;

import com.example.MpApp.dto.task.TaskResponse;
import com.example.MpApp.entity.enums.TaskStatus;
import com.example.MpApp.entity.enums.TaskType;
import com.example.MpApp.entity.task.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("""
        SELECT new com.example.MpApp.dto.task.TaskResponse(
            t.id, t.title, t.description, t.assignedDate, t.deadline,
            t.progress, t.estimatedHours, t.status, t.priority, t.taskType,
            s.id, s.name, s.role, s.staffId, tl.id, tl.name, tl.teamLeadId
        )
        FROM Task t
        LEFT JOIN t.staff s
        LEFT JOIN t.teamLead tl
        WHERE s.id = :staffId
    """)
    List<TaskResponse> findTasksByStaff(Long staffId);

    @Query("""
        SELECT new com.example.MpApp.dto.task.TaskResponse(
            t.id, t.title, t.description, t.assignedDate, t.deadline,
            t.progress, t.estimatedHours, t.status, t.priority, t.taskType,
            s.id, s.name, s.role, s.staffId, tl.id, tl.name, tl.teamLeadId
        )
        FROM Task t
        LEFT JOIN t.staff s
        LEFT JOIN t.teamLead tl
        WHERE s.id = :staffId
        AND t.taskType = :taskType
    """)
    List<TaskResponse> findTasksByStaffAndType(Long staffId, TaskType taskType);

    @Query("""
        SELECT new com.example.MpApp.dto.task.TaskResponse(
            t.id, t.title, t.description, t.assignedDate, t.deadline,
            t.progress, t.estimatedHours, t.status, t.priority, t.taskType,
            s.id, s.name, s.role, s.staffId, tl.id, tl.name, tl.teamLeadId
        )
        FROM Task t
        LEFT JOIN t.staff s
        LEFT JOIN t.teamLead tl
        WHERE t.status = :status
    """)
    List<TaskResponse> findTasksByStatus(TaskStatus status);

    @Query("""
        SELECT new com.example.MpApp.dto.task.TaskResponse(
            t.id, t.title, t.description, t.assignedDate, t.deadline,
            t.progress, t.estimatedHours, t.status, t.priority, t.taskType,
            s.id, s.name, s.role, s.staffId, tl.id, tl.name, tl.teamLeadId
        )
        FROM Task t
        LEFT JOIN t.staff s
        LEFT JOIN t.teamLead tl
        WHERE t.deadline = :deadline
    """)
    List<TaskResponse> findTasksByDeadline(LocalDate deadline);

    @Query("""
        SELECT new com.example.MpApp.dto.task.TaskResponse(
            t.id, t.title, t.description, t.assignedDate, t.deadline,
            t.progress, t.estimatedHours, t.status, t.priority, t.taskType,
            s.id, s.name, s.role, s.staffId, tl.id, tl.name, tl.teamLeadId
        )
        FROM Task t
        LEFT JOIN t.staff s
        LEFT JOIN t.teamLead tl
        WHERE t.assignedDate BETWEEN :startDate AND :endDate
    """)
    List<TaskResponse> findTasksBetweenDates(LocalDate startDate, LocalDate endDate);

    @Query("""
        SELECT new com.example.MpApp.dto.task.TaskResponse(
            t.id, t.title, t.description, t.assignedDate, t.deadline,
            t.progress, t.estimatedHours, t.status, t.priority, t.taskType,
            s.id, s.name, s.role, s.staffId, tl.id, tl.name, tl.teamLeadId
        )
        FROM Task t
        LEFT JOIN t.staff s
        LEFT JOIN t.teamLead tl
        WHERE tl.id = :teamLeadId
    """)
    List<TaskResponse> findTasksByTeamLead(Long teamLeadId);

    @Query("""
        SELECT new com.example.MpApp.dto.task.TaskResponse(
            t.id, t.title, t.description, t.assignedDate, t.deadline,
            t.progress, t.estimatedHours, t.status, t.priority, t.taskType,
            s.id, s.name, s.role, s.staffId, tl.id, tl.name, tl.teamLeadId
        )
        FROM Task t
        LEFT JOIN t.staff s
        LEFT JOIN t.teamLead tl
        WHERE t.id = :taskId
    """)
    TaskResponse findTaskById(Long taskId);

    @Query("""
        SELECT new com.example.MpApp.dto.task.TaskResponse(
            t.id, t.title, t.description, t.assignedDate, t.deadline,
            t.progress, t.estimatedHours, t.status, t.priority, t.taskType,
            s.id, s.name, s.role, s.staffId, tl.id, tl.name, tl.teamLeadId
        )
        FROM Task t
        LEFT JOIN t.staff s
        LEFT JOIN t.teamLead tl
    """)
    List<TaskResponse> findAllTasks();

    @Query("""
        SELECT t
        FROM Task t
        WHERE t.status = :status
    """)
    List<Task> findTasksByStatusEntity(TaskStatus status);

    List<Task> findByStaff_Id(Long staffId);
    List<Task> findByStatusIn(List<TaskStatus> statuses);

    long countByStaffId(Long staffId);

    long countByStaffIdAndStatus(Long staffId, TaskStatus status);

    @Query("SELECT CASE WHEN COUNT(t) = 0 THEN 0.0 ELSE " +
            "(SUM(CASE WHEN r.verificationStatus = 'APPROVED' THEN 1.0 ELSE 0.0 END) / COUNT(r)) " +
            "END FROM TaskReview r JOIN r.task t WHERE t.staff.id = :staffId")
    Double calculateApprovalRate(@Param("staffId") Long staffId);

    void deleteByStaffId(Long staffId);
}