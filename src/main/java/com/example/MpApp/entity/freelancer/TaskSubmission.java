package com.example.MpApp.entity.freelancer;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "task_submission")
@Data
public class TaskSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "freelancer_task_id", unique = true)
    private FreelancerTask freelancerTask;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    // constructors, getters, setters
}