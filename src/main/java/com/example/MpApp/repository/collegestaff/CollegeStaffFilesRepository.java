package com.example.MpApp.repository.collegestaff;

import com.example.MpApp.entity.collegestaff.CollegeStaffFiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollegeStaffFilesRepository extends JpaRepository<CollegeStaffFiles,Long> {
    List<CollegeStaffFiles> findByStaffId(Long staffId);

}
