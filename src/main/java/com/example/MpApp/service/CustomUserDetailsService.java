package com.example.MpApp.service;

import com.example.MpApp.repository.admin.AdminRepository;
import com.example.MpApp.repository.officestaff.OfficeStaffRepository;
import com.example.MpApp.repository.teamlead.TeamLeadRepository;
import com.example.MpApp.repository.collegestaff.CollegeStaffRepository;
import com.example.MpApp.repository.student.StudentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private CollegeStaffRepository collegeStaffRepository;

    @Autowired
    private TeamLeadRepository teamLeadRepository;

    @Autowired
    private OfficeStaffRepository officeStaffRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        String password = null;

        if (adminRepository.findByEmail(email).isPresent()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            password = adminRepository.findByEmail(email).get().getPassword();
        }
        if (collegeStaffRepository.findByEmail(email).isPresent()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_COLLEGE_STAFF"));
            if (password == null) password = collegeStaffRepository.findByEmail(email).get().getPassword();
        }
        if (teamLeadRepository.findByEmail(email).isPresent()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_TEAM_LEAD"));
            if (password == null) password = teamLeadRepository.findByEmail(email).get().getPassword();
        }
        if (officeStaffRepository.findByEmail(email).isPresent()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_OFFICE_STAFF"));
            if (password == null) password = officeStaffRepository.findByEmail(email).get().getPassword();
        }
        if (studentRepository.findByEmail(email).isPresent()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_STUDENT"));
            if (password == null) password = studentRepository.findByEmail(email).get().getPassword();
        }

        if (authorities.isEmpty()) {
            throw new UsernameNotFoundException("User identity not found across system registries for: " + email);
        }

        return User.builder()
                .username(email)
                .password(password)
                .authorities(authorities)
                .build();
    }
}