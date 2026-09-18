package com.example.MpApp.service.freelancer;

import com.example.MpApp.dto.Freelancer.FreelancerTaskRequestDTO;
import com.example.MpApp.dto.Freelancer.FreelancerTaskResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface FreelancerTaskService {

    FreelancerTaskResponseDTO create(
            FreelancerTaskRequestDTO dto,
            MultipartFile syllabus
    );

    FreelancerTaskResponseDTO update(
            Long id,
            FreelancerTaskRequestDTO dto,
            MultipartFile syllabus
    );

    FreelancerTaskResponseDTO getById(Long id);

    List<FreelancerTaskResponseDTO> getAll();

    void delete(Long id);

    List<FreelancerTaskResponseDTO> getByFreelancerId(
            Long freelancerId
    );
}