package com.example.MpApp.entity.developer_trainer_staff;

import com.example.MpApp.entity.officestaff.OfficeStaff;
import com.example.MpApp.entity.student.Student;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="cash_fee_confirmation")
@Getter
@Setter
public class CashFeeConfirmation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Student student;

    @ManyToOne
    private TrainingBatch batch;

    @ManyToOne
    private OfficeStaff trainer;

    private Double amount;

    private String remarks;

    private String status;

    private LocalDateTime confirmedAt;

    @PrePersist
    public void prePersist() {
        confirmedAt = LocalDateTime.now();
    }
}