package com.example.MpApp.service.freelancer;

import com.example.MpApp.dto.Freelancer.FreelancerTaskRequestDTO;
import com.example.MpApp.dto.Freelancer.FreelancerTaskResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    Page<FreelancerTaskResponseDTO> getAll(Pageable pageable);

    void delete(Long id);

    List<FreelancerTaskResponseDTO> getByFreelancerId(
            Long freelancerId
    );
}