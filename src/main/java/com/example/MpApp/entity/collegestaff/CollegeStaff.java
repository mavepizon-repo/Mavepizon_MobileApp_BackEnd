package com.example.MpApp.entity.collegestaff;

import jakarta.persistence.*;

@Entity
@Table(name = "college_staff")
public class CollegeStaff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String collegeName;

    private String department;

    private String gender;

    @Column(unique = true)
    private String email;

    private String password;

    private String mobileNumber;

    // --- ADDED FIELD ---
    @Column(columnDefinition = "integer default 0")
    private Integer uploadedStudentsCount = 0;

    public CollegeStaff() {
    }

    // ... [Keep your existing getters and setters] ...

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCollegeName() { return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    // --- ADDED GETTERS AND SETTERS ---
    public Integer getUploadedStudentsCount() {
        return uploadedStudentsCount;
    }

    public void setUploadedStudentsCount(Integer uploadedStudentsCount) {
        this.uploadedStudentsCount = uploadedStudentsCount;
    }
}