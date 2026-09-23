package com.example.MpApp.entity.freelancer;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "techstack")
@Data
public class TechStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    public TechStack(String name) {
        this.name = name;
    }

    public TechStack() {

    }

    // constructors, getters, setters
}