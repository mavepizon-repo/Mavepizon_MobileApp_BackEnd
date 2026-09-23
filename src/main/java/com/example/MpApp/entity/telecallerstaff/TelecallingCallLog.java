package com.example.MpApp.entity.telecallerstaff;


import com.example.MpApp.entity.officestaff.OfficeStaff;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class TelecallingCallLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private OfficeStaff staff;

    @ManyToOne
    private TelecallingEnquiry enquiry;

    private LocalDateTime callTime ;

    private LocalDateTime answeredTime;

    private LocalDateTime endTime;

    private Long durationSeconds;

    private String callStatus;

    private LocalDate createdDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDate.now();
    }




}
