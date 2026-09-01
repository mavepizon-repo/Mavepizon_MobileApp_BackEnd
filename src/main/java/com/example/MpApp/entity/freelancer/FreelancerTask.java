package com.example.MpApp.entity.freelancer;

import com.example.MpApp.entity.freelancer.TaskStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class FreelancerTask {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String orgName; // college or company name

        private Integer noOfDays;

        private LocalDate startDate;

        private LocalDate endDate;

        private String meetingLink;

        private String meetingEmail;

        private String meetingPassword; // plain text

        private String department;

        private String domain;

        private Integer noOfStudents;

        @Column(columnDefinition = "TEXT")
        private String syllabus;

        @Enumerated(EnumType.STRING)
        private TaskStatus status;

        @ManyToMany
        @JoinTable(
                name = "task_freelancer",
                joinColumns = @JoinColumn(name = "task_id"),
                inverseJoinColumns = @JoinColumn(name = "freelancer_id")
        )
        private List<Freelancer> freelancers = new ArrayList<>();

        // constructors, getters, setters

}
