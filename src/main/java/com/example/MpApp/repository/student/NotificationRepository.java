package com.example.MpApp.repository.student;

import com.example.MpApp.entity.student.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
    List<Notification> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<Notification> findByStudentId(Long studentId);
}
