package com.example.MpApp.service;

import com.example.MpApp.entity.freelancer.Freelancer;
import com.example.MpApp.repository.admin.AdminRepository;
import com.example.MpApp.repository.freelancer.FreelancerRepository;
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

    @Autowired
    private FreelancerRepository freelancerRepository;

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
        if (freelancerRepository.findByEmail(email).isPresent()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_FREELANCER"));
            if (password == null) password = freelancerRepository.findByEmail(email).get().getPassword();
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

    public boolean isAccountActive(String email, String role) {
        return switch (role) {
            case "ADMIN" -> adminRepository.findByEmail(email)
                    .map(a -> Boolean.TRUE.equals(a.getActive()))
                    .orElse(false);
            case "COLLEGE_STAFF" -> collegeStaffRepository.findByEmail(email)
                    .map(s -> Boolean.TRUE.equals(s.getActive()))
                    .orElse(false);
            case "TEAM_LEAD" -> teamLeadRepository.findByEmail(email)
                    .map(s -> s.isActive())
                    .orElse(false);
            case "OFFICE_STAFF" -> officeStaffRepository.findByEmail(email)
                    .map(s -> s.isActive() && "APPROVED".equals(s.getApprovalStatus()))
                    .orElse(false);
            case "FREELANCER" -> freelancerRepository.findByEmail(email)
                    .map(f -> Boolean.TRUE.equals(f.getActive()))
                    .orElse(false);
            case "STUDENT" -> studentRepository.findByEmail(email).isPresent();
            default -> false;
        };
    }

    /**
     * Current token counter for an account, used to reject tokens that were
     * issued before a password change. Resolved per role because the same
     * address can hold several independent accounts.
     *
     * @return the stored counter, or -1 when no such account exists
     */
    public int getTokenVersion(String email, String role) {
        return switch (role) {
            case "ADMIN" -> adminRepository.findByEmail(email)
                    .map(a -> a.getTokenVersion())
                    .orElse(-1);
            case "COLLEGE_STAFF" -> collegeStaffRepository.findByEmail(email)
                    .map(s -> s.getTokenVersion())
                    .orElse(-1);
            case "TEAM_LEAD" -> teamLeadRepository.findByEmail(email)
                    .map(s -> s.getTokenVersion())
                    .orElse(-1);
            case "OFFICE_STAFF" -> officeStaffRepository.findByEmail(email)
                    .map(s -> s.getTokenVersion())
                    .orElse(-1);
            case "FREELANCER" -> freelancerRepository.findByEmail(email)
                    .map(f -> f.getTokenVersion())
                    .orElse(-1);
            case "STUDENT" -> studentRepository.findByEmail(email)
                    .map(s -> s.getTokenVersion())
                    .orElse(-1);
            default -> -1;
        };
    }
}