    package com.example.MpApp.entity.officestaff;
    
    import java.time.LocalDate;

    import com.example.MpApp.entity.admin.Admin;
    import com.example.MpApp.entity.common.StaffCreator;
    import com.example.MpApp.entity.enums.StaffCategory;
    import com.example.MpApp.entity.teamlead.TeamLead;
    import com.fasterxml.jackson.annotation.JsonProperty;
    import jakarta.persistence.*;
    import lombok.Data;
    import org.hibernate.annotations.*;



    @Entity
    @Table(name = "office_staff")
    @Data
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

        @Any(fetch = FetchType.LAZY)
        @AnyDiscriminator(DiscriminatorType.STRING)
        @AnyDiscriminatorValues({
                @AnyDiscriminatorValue(discriminator = "ADMIN", entity = Admin.class),
                @AnyDiscriminatorValue(discriminator = "TEAM_LEAD", entity = TeamLead.class)
        })
        @AnyKeyJavaClass(Long.class)
        @Column(name = "created_by_type")
        @JoinColumn(name = "created_by_id")
        private StaffCreator createdBy;
    
        private LocalDate joiningDate;
    
        private String nativePlace;
        private String degree;
    
        private Integer yearPassedOut;
        // Inside OfficeStaff.java
        private String approvalStatus = "PENDING"; // Values: PENDING, APPROVED, REJECTED
        private String mobileNumber;
        private String email;

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        private String password;
    
        private String aadhaarFile;
        private String profilePhoto;
        private String resumeFile;
    
        private Integer experience;
        private String previousCompany;
        private String skills;

        private String employeeId; // New
        private String bloodGroup; // New
        private String branchName; // New
        private String experienceCertificate;
        private boolean active;
    
        @Enumerated(EnumType.STRING)
        private StaffCategory category;
    
        private Integer score = 100;
    

    }
