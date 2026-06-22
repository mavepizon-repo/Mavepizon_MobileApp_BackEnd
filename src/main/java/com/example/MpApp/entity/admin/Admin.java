package com.example.MpApp.entity.admin;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admin")
@NoArgsConstructor
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userName;

    @Column(unique = true)
    private String email;

    private String password;

    private String phone_no;

    public Admin(Long id, String userName, String email, String password, String phone_no) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.phone_no = phone_no;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone_no() {
        return phone_no;
    }

    public void setPhone_no(String phone_no) {
        this.phone_no = phone_no;
    }
}
