package com.example.MpApp.service.freelancer;

import com.example.MpApp.dto.Freelancer.FreelancerRequestDTO;
import com.example.MpApp.dto.Freelancer.FreelancerResponseDTO;
import com.example.MpApp.entity.freelancer.Freelancer;
import com.example.MpApp.entity.freelancer.TechStack;
import com.example.MpApp.repository.freelancer.FreelancerRepository;
import com.example.MpApp.repository.freelancer.TechStackRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FreelancerServiceImpl implements FreelancerService {

    private final FreelancerRepository freelancerRepository;
    private final TechStackRepository techStackRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public FreelancerServiceImpl(FreelancerRepository freelancerRepository,
                                 TechStackRepository techStackRepository, BCryptPasswordEncoder passwordEncoder) {
        this.freelancerRepository = freelancerRepository;
        this.techStackRepository = techStackRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public FreelancerResponseDTO create(FreelancerRequestDTO dto) {
        Freelancer freelancer = new Freelancer();
        mapDtoToEntity(dto, freelancer);
        if(freelancer.getPassword() != null){
            freelancer.setPassword(passwordEncoder.encode(dto.getPassword()));
        }else{
            freelancer.setPassword(passwordEncoder.encode(dto.getEmail()));
        }

        Freelancer saved = freelancerRepository.save(freelancer);
        return mapEntityToDto(saved);
    }

    @Override
    public FreelancerResponseDTO update(Long id, FreelancerRequestDTO dto) {
        Freelancer freelancer = freelancerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Freelancer not found: " + id));
        mapDtoToEntity(dto, freelancer);
        Freelancer updated = freelancerRepository.save(freelancer);
        return mapEntityToDto(updated);
    }

    @Override
    public FreelancerResponseDTO getById(Long id) {
        Freelancer freelancer = freelancerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Freelancer not found: " + id));
        return mapEntityToDto(freelancer);
    }

    @Override
    public List<FreelancerResponseDTO> getAll() {
        return freelancerRepository.findAll().stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        freelancerRepository.deleteById(id);
    }

    @Override
    public List<FreelancerResponseDTO> filterByDistrict(String district) {
        return freelancerRepository.findByDistrictIgnoreCase(district).stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<FreelancerResponseDTO> filterByTechStack(String techStackName) {
        return freelancerRepository.findByTechStackName(techStackName).stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    private void mapDtoToEntity(FreelancerRequestDTO dto, Freelancer freelancer) {
        freelancer.setName(dto.getName());
        freelancer.setYearOfPassing(dto.getYearOfPassing());
        freelancer.setExperience(dto.getExperience());
        freelancer.setDistrict(dto.getDistrict());
        freelancer.setAddress(dto.getAddress());
        freelancer.setMobileNo(dto.getMobileNo());
        freelancer.setEmail(dto.getEmail());
        freelancer.setResume(dto.getResume());
        freelancer.setAadhaar(dto.getAadhaar());

        List<TechStack> techStacks = dto.getTechStackNames().stream()
                .map(name -> techStackRepository.findByNameIgnoreCase(name)
                        .orElseGet(() -> techStackRepository.save(new TechStack(name))))
                .collect(Collectors.toList());
        freelancer.setTechStacks(techStacks);
    }

    private FreelancerResponseDTO mapEntityToDto(Freelancer freelancer) {
        FreelancerResponseDTO dto = new FreelancerResponseDTO();
        dto.setId(freelancer.getId());
        dto.setName(freelancer.getName());
        dto.setYearOfPassing(freelancer.getYearOfPassing());
        dto.setExperience(freelancer.getExperience());
        dto.setDistrict(freelancer.getDistrict());
        dto.setAddress(freelancer.getAddress());
        dto.setMobileNo(freelancer.getMobileNo());
        dto.setEmail(freelancer.getEmail());
        dto.setResume(freelancer.getResume());
        dto.setAadhaar(freelancer.getAadhaar());
        dto.setTechStackNames(freelancer.getTechStacks().stream()
                .map(TechStack::getName)
                .collect(Collectors.toList()));
        return dto;
    }
}