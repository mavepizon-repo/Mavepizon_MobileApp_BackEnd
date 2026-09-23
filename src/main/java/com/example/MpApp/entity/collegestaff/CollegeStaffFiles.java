package com.example.MpApp.entity.collegestaff;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CollegeStaffFiles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    @JoinColumn(name = "staff_id")
    private CollegeStaff staff;
    private String CourseURL;
    private String ProposalURL;

}
