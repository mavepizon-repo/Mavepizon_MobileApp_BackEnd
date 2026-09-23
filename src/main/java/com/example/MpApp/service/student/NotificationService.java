package com.example.MpApp.service.student;

import com.example.MpApp.entity.student.Notification;
import com.example.MpApp.entity.student.Student;
import com.example.MpApp.repository.student.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import com.example.MpApp.repository.student.StudentRepository;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final StudentRepository studentRepository;

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

    public List<Notification> getNotificationsByStudentEmail(String email) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student Not Found"));
        return getNotificationsByStudent(student.getId());
    }

    // Mark a specific notification as read
    @Transactional
    public void markAsRead(Long notificationId, String email) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student Not Found"));
        Notification note = notificationRepository.findByIdAndStudentId(notificationId, student.getId())
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        note.setRead(true);
        notificationRepository.save(note);
    }
}
