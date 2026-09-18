package com.example.MpApp.service.student;

import com.example.MpApp.entity.student.Notification;
import com.example.MpApp.entity.student.Student;
import com.example.MpApp.repository.student.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public void sendNotification(Student student, String message) {
        Notification notification = new Notification();
        notification.setStudent(student);
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsByStudent(Long studentId) {
        return notificationRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    // Mark a specific notification as read
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification note = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        note.setRead(true);
        notificationRepository.save(note);
    }
}
