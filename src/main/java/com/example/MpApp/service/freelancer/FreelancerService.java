package com.example.MpApp.service.freelancer;

import com.example.MpApp.dto.Freelancer.FreelancerRequestDTO;
import com.example.MpApp.dto.Freelancer.FreelancerResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FreelancerService {
    FreelancerResponseDTO create(FreelancerRequestDTO dto);
    FreelancerResponseDTO update(Long id, FreelancerRequestDTO dto);
    FreelancerResponseDTO getById(Long id);
    List<FreelancerResponseDTO> getAll();
    void delete(Long id);
    List<FreelancerResponseDTO> filterByDistrict(String district);
    List<FreelancerResponseDTO> filterByTechStack(String techStackName);
}