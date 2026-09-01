package com.example.MpApp.service.freelancer;

import com.example.MpApp.config.JwtService;
import com.example.MpApp.dto.Freelancer.FreelancerRequestDTO;
import com.example.MpApp.dto.Freelancer.FreelancerResponseDTO;
import com.example.MpApp.dto.Freelancer.FreelancerTaskResponseDTO;
import com.example.MpApp.dto.Freelancer.LoginRequestDto;
import com.example.MpApp.entity.freelancer.Freelancer;
import com.example.MpApp.entity.freelancer.FreelancerTask;
import com.example.MpApp.entity.freelancer.TechStack;
import com.example.MpApp.exception.ResourceNotFoundException;
import com.example.MpApp.repository.freelancer.FreelancerRepository;
import com.example.MpApp.repository.freelancer.FreelancerTaskRepository;
import com.example.MpApp.repository.freelancer.TechStackRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FreelancerServiceImpl implements FreelancerService {

    private final FreelancerRepository freelancerRepository;
    private final TechStackRepository techStackRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final FreelancerTaskRepository freelancerTaskRepository;

    public FreelancerServiceImpl(FreelancerRepository freelancerRepository,
                                 TechStackRepository techStackRepository, BCryptPasswordEncoder passwordEncoder, JwtService jwtService, FreelancerTaskRepository freelancerTaskRepository) {
        this.freelancerRepository = freelancerRepository;
        this.techStackRepository = techStackRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.freelancerTaskRepository = freelancerTaskRepository;
    }

    public String extractEmail(String authHeader){
        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            throw new RuntimeException("Token Required");
        }

        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);

        return email;
    }

    @Override
    public FreelancerResponseDTO create(FreelancerRequestDTO dto) {
        Freelancer freelancer = new Freelancer();
        mapDtoToEntity(dto, freelancer);
        if(dto.getPassword() != null){
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
    public Map<String, String> loginFreelancer(LoginRequestDto request) {

        Freelancer freelancer = freelancerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer not found"));

        if (!passwordEncoder.matches(request.getPassword(), freelancer.getPassword())) {
            return Map.of("message", "Invalid Password");
        }

        UserDetails userDetails = User.builder()
                .username(freelancer.getEmail())
                .password(freelancer.getPassword())
                .roles("FREELANCER")
                .build();

        String token = jwtService.generateToken(userDetails);

        String role = "FREELANCER";

        return Map.of(
                "freelancerId", freelancer.getId().toString(),
                "email", freelancer.getEmail(),
                "name", freelancer.getName(),
                "token", token,
                "role", role
        );
    }

    @Override
    public List<FreelancerTaskResponseDTO> getMyTasks(String authHeader) {
        String email = extractEmail(authHeader);

        Freelancer freelancer = freelancerRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Freelancer not found")
        );

        List<FreelancerTask> tasks = freelancerTaskRepository.findByFreelancerId(freelancer.getId());

        return tasks.stream()
                .map(this::mapTaskEntityToDto)
                .collect(Collectors.toList());

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

    private FreelancerTaskResponseDTO mapTaskEntityToDto(FreelancerTask task) {
        FreelancerTaskResponseDTO dto = new FreelancerTaskResponseDTO();
        dto.setId(task.getId());
        dto.setOrgName(task.getOrgName());
        dto.setNoOfDays(task.getNoOfDays());
        dto.setStartDate(task.getStartDate());
        dto.setEndDate(task.getEndDate());
        dto.setMeetingLink(task.getMeetingLink());
        dto.setMeetingEmail(task.getMeetingEmail());
        dto.setMeetingPassword(task.getMeetingPassword());
        dto.setDepartment(task.getDepartment());
        dto.setDomain(task.getDomain());
        dto.setNoOfStudents(task.getNoOfStudents());
        dto.setSyllabus(task.getSyllabus());
        dto.setStatus(task.getStatus());
        dto.setFreelancerIds(task.getFreelancers().stream().map(Freelancer::getId).collect(Collectors.toList()));
        dto.setFreelancerNames(task.getFreelancers().stream().map(Freelancer::getName).collect(Collectors.toList()));
        return dto;
    }
}