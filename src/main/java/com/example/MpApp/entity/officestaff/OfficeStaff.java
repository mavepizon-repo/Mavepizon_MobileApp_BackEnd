    package com.example.MpApp.entity.officestaff;
    
    import java.time.LocalDate;
    
    import com.example.MpApp.entity.enums.StaffCategory;
    import jakarta.persistence.*;
    
    @Entity
    @Table(name = "office_staff")
    public class OfficeStaff {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
    
        @Column(unique = true)
        private String staffId;
    
        private String branch;
    
        private String name;
        private String gender;
        private String role;
    
        private LocalDate joiningDate;
    
        private String nativePlace;
        private String degree;
    
        private Integer yearPassedOut;
    
        private String mobileNumber;
        private String email;
        private String password;
    
        private String aadhaarFile;
        private String profilePhoto;
        private String resumeFile;
    
        private Integer experience;
        private String previousCompany;

        private String employeeId; // New
        private String bloodGroup; // New
        private String branchName; // New
        private String experienceCertificate;
    
        @Enumerated(EnumType.STRING)
        private StaffCategory category;
    
        private Integer score = 100;
    
        public Long getId() {
            return id;
        }
    
        public String getResumeFile() {
            return resumeFile;
        }
    
        public String getSkills() {
            return skills;
        }
    
        public void setSkills(String skills) {
            this.skills = skills;
        }
    
        public String getPreviousCompany() {
            return previousCompany;
        }
    
        public void setPreviousCompany(String previousCompany) {
            this.previousCompany = previousCompany;
        }
    
        public Integer getExperience() {
            return experience;
        }
    
        public void setExperience(Integer experience) {
            this.experience = experience;
        }
    
        public void setResumeFile(String resumeFile) {
            this.resumeFile = resumeFile;
        }
    
        public String getProfilePhoto() {
            return profilePhoto;
        }
    
        public void setProfilePhoto(String profilePhoto) {
            this.profilePhoto = profilePhoto;
        }
    
        public String getAadhaarFile() {
            return aadhaarFile;
        }
    
        public void setAadhaarFile(String aadhaarFile) {
            this.aadhaarFile = aadhaarFile;
        }
    
        public String getPassword() {
            return password;
        }
    
        public void setPassword(String password) {
            this.password = password;
        }
    
        public String getEmail() {
            return email;
        }
    
        public void setEmail(String email) {
            this.email = email;
        }
    
        public String getMobileNumber() {
            return mobileNumber;
        }
    
        public void setMobileNumber(String mobileNumber) {
            this.mobileNumber = mobileNumber;
        }
    
        public String getDegree() {
            return degree;
        }
    
        public void setDegree(String degree) {
            this.degree = degree;
        }
    
        public Integer getYearPassedOut() {
            return yearPassedOut;
        }
    
        public void setYearPassedOut(Integer yearPassedOut) {
            this.yearPassedOut = yearPassedOut;
        }
    
        public String getNativePlace() {
            return nativePlace;
        }
    
        public void setNativePlace(String nativePlace) {
            this.nativePlace = nativePlace;
        }
    
        public void setId(Long id) {
            this.id = id;
        }
    
        public String getRole() {
            return role;
        }
    
        public LocalDate getJoiningDate() {
            return joiningDate;
        }
    
        public void setJoiningDate(LocalDate joiningDate) {
            this.joiningDate = joiningDate;
        }
    
        public void setRole(String role) {
            this.role = role;
        }
    
        @Column(length = 1000)
        private String skills;
    
        public String getGender() {
            return gender;
        }
    
        public void setGender(String gender) {
            this.gender = gender;
        }
    
        public String getName() {
            return name;
        }
    
        public void setName(String name) {
            this.name = name;
        }
    
        public StaffCategory getCategory() {
            return category;
        }
    
        public void setCategory(StaffCategory category) {
            this.category = category;
        }
    
        public Integer getScore() {
            return score;
        }
    
        public void setScore(Integer score) {
            this.score = score;
        }
    
        public String getStaffId() {
            return staffId;
        }
    
        public void setStaffId(String staffId) {
            this.staffId = staffId;
        }
    
        public String getBranch() {
            return branch;
        }
    
        public void setBranch(String branch) {
            this.branch = branch;
        }
    
        public OfficeStaff() {
        }

        public String getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(String employeeId) {
            this.employeeId = employeeId;
        }

        public String getBloodGroup() {
            return bloodGroup;
        }

        public void setBloodGroup(String bloodGroup) {
            this.bloodGroup = bloodGroup;
        }

        public String getBranchName() {
            return branchName;
        }

        public void setBranchName(String branchName) {
            this.branchName = branchName;
        }

        public String getExperienceCertificate() {
            return experienceCertificate;
        }

        public void setExperienceCertificate(String experienceCertificate) {
            this.experienceCertificate = experienceCertificate;
        }
        // Generate Getters and Setters
    }
