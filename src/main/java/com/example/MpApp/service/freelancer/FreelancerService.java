package com.example.MpApp.service.freelancer;

import com.example.MpApp.dto.Freelancer.FreelancerRequestDTO;
import com.example.MpApp.dto.Freelancer.FreelancerResponseDTO;
import com.example.MpApp.dto.Freelancer.FreelancerTaskResponseDTO;
import com.example.MpApp.dto.Freelancer.LoginRequestDto;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface FreelancerService {

    FreelancerResponseDTO create(
            FreelancerRequestDTO dto,
            MultipartFile profile,
            MultipartFile aadhaar,
            MultipartFile resume
    );

    FreelancerResponseDTO update(
            Long id,
            FreelancerRequestDTO dto,
            MultipartFile profile,
            MultipartFile aadhaar,
            MultipartFile resume
    );

    FreelancerResponseDTO getById(Long id);

    Page<FreelancerResponseDTO> getAll(Pageable pageable);

    void delete(Long id);

    List<FreelancerResponseDTO> filterByDistrict(String district);

    List<FreelancerResponseDTO> filterByTechStack(String techStackName);

    Map<String, String> loginFreelancer(LoginRequestDto request);

    List<FreelancerTaskResponseDTO> getMyTasks(String authHeader);
}