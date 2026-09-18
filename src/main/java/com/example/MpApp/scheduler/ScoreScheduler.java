package com.example.MpApp.scheduler;

import com.example.MpApp.entity.officestaff.OfficeStaff;
import com.example.MpApp.entity.task.Task;
import com.example.MpApp.entity.enums.TaskStatus;
import com.example.MpApp.repository.officestaff.OfficeStaffRepository;
import com.example.MpApp.repository.task.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ScoreScheduler {

    private final TaskRepository taskRepository;
    private final OfficeStaffRepository officeStaffRepository;

    @Scheduled(cron = "0 0 18 * * ?")
    public void deductScoreForPendingTasks() {

        List<Task> tasks =
                taskRepository.findByStatusIn(
                        List.of(
                                TaskStatus.PENDING,
                                TaskStatus.REWORK_REQUIRED
                        ));

        for (Task task : tasks) {

            OfficeStaff staff = task.getStaff();

            if (staff == null) {
                continue;
            }

            int updatedScore =
                    Math.max(
                            0,
                            staff.getScore() - 2
                    );

            staff.setScore(updatedScore);

            officeStaffRepository.save(staff);
        }
    }
}