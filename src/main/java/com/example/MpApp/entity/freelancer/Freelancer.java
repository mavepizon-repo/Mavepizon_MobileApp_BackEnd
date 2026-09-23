package com.example.MpApp.entity.freelancer;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "freelancer")
@Data
public class Freelancer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer yearOfPassing;

    private Double experience;

    private String district;

    private String address;

    private String mobileNo;

    @Column(unique = true)
    private String email;

    private String password;

    // Cloudinary URL
    private String profile;

    // Cloudinary URL
    private String resume;

    // Cloudinary URL
    @Column(unique = true)
    private String aadhaar;

    @Column(nullable = false)
    private Boolean active = true;

    @ManyToMany
    @JoinTable(
            name = "freelancer_techstack",
            joinColumns = @JoinColumn(name = "freelancer_id"),
            inverseJoinColumns = @JoinColumn(name = "techstack_id")
    )
    private List<TechStack> techStacks = new ArrayList<>();

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}