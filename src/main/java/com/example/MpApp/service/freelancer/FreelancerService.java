package com.example.MpApp.service.freelancer;

import com.example.MpApp.dto.Freelancer.FreelancerRequestDTO;
import com.example.MpApp.dto.Freelancer.FreelancerResponseDTO;
import com.example.MpApp.dto.Freelancer.FreelancerTaskResponseDTO;
import com.example.MpApp.dto.Freelancer.LoginRequestDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface FreelancerService {
    FreelancerResponseDTO create(FreelancerRequestDTO dto);
    FreelancerResponseDTO update(Long id, FreelancerRequestDTO dto);
    FreelancerResponseDTO getById(Long id);
    List<FreelancerResponseDTO> getAll();
    void delete(Long id);
    List<FreelancerResponseDTO> filterByDistrict(String district);
    List<FreelancerResponseDTO> filterByTechStack(String techStackName);
    Map<String, String> loginFreelancer(LoginRequestDto request);
    List<FreelancerTaskResponseDTO> getMyTasks(String authHeader);
}