package com.example.MpApp.dto.collegestaff;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollegeStaffFileDTO {

    private Long staffId;
    private String staffName;
    private String collegeName;
    private String syllabusURL;
    private String proposalURL;
}
