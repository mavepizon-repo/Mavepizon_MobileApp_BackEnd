package com.example.MpApp.entity.certificate;

import com.example.MpApp.entity.course.StudentCourseRegistration;
import com.example.MpApp.entity.developer_trainer_staff.TrainingBatch;
import com.example.MpApp.entity.internship.InternshipRegistration;
import com.example.MpApp.entity.student.Student;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "certificates")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================================
    // LAZY MAPPINGS WITH RECURSION GUARDS
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    // Ignores the nested lists inside the Student entity to stop the loop
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "registrations", "certificates", "batchStudents"})
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_batch_id")
    // Ignores the nested details of the batch to keep the JSON clean
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "trainer", "offeredCourse", "students"})
    private TrainingBatch trainingBatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_registration_id")
    // Ignores the back-references inside the registration
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "student", "offeredCourse"})
    private StudentCourseRegistration courseRegistration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_registration_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "student", "internship"})
    private InternshipRegistration internshipRegistration;

    // ==========================================
    // CERTIFICATE DETAILS
    // ==========================================

    // "COURSE" or "INTERNSHIP"
    private String recordType;

    // "PENDING", "PROCESSING", "ISSUED"
    private String status = "PENDING";

    private String fileUrl;

    private LocalDate issueDate;

    public Certificate() {}

    // ==========================================
    // GETTERS & SETTERS
    // ==========================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public TrainingBatch getTrainingBatch() { return trainingBatch; }
    public void setTrainingBatch(TrainingBatch trainingBatch) { this.trainingBatch = trainingBatch; }

    public StudentCourseRegistration getCourseRegistration() { return courseRegistration; }
    public void setCourseRegistration(StudentCourseRegistration courseRegistration) { this.courseRegistration = courseRegistration; }

    public InternshipRegistration getInternshipRegistration() { return internshipRegistration; }
    public void setInternshipRegistration(InternshipRegistration internshipRegistration) { this.internshipRegistration = internshipRegistration; }

    public String getRecordType() { return recordType; }
    public void setRecordType(String recordType) { this.recordType = recordType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
}