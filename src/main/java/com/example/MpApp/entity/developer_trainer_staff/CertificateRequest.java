package com.example.MpApp.entity.developer_trainer_staff;

import com.example.MpApp.entity.officestaff.OfficeStaff;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="certificate_requests")
@Getter
@Setter
public class CertificateRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private TrainingBatch batch;

    @ManyToOne
    private OfficeStaff trainer;

    private String status;

    private LocalDateTime requestedAt;

    @PrePersist
    public void prePersist() {
        requestedAt = LocalDateTime.now();

        if(status == null)
            status = "PENDING";
    }
}