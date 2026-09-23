package com.example.MpApp.service.freelancer;

import com.example.MpApp.security.PasswordPolicy;

import com.example.MpApp.config.JwtService;
import com.example.MpApp.dto.Freelancer.FreelancerRequestDTO;
import com.example.MpApp.dto.Freelancer.FreelancerResponseDTO;
import com.example.MpApp.dto.Freelancer.FreelancerTaskResponseDTO;
import com.example.MpApp.dto.Freelancer.LoginRequestDto;
import com.example.MpApp.entity.freelancer.Freelancer;
import com.example.MpApp.entity.freelancer.FreelancerTask;
import com.example.MpApp.entity.freelancer.TechStack;
import com.example.MpApp.exception.ResourceNotFoundException;
import com.example.MpApp.exception.InvalidCredentialsException;
import com.example.MpApp.repository.freelancer.FreelancerRepository;
import com.example.MpApp.repository.freelancer.FreelancerTaskRepository;
import com.example.MpApp.repository.freelancer.TechStackRepository;
import com.example.MpApp.service.CloudinaryService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

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
    private final CloudinaryService cloudinaryService;

    public FreelancerServiceImpl(
            FreelancerRepository freelancerRepository,
            TechStackRepository techStackRepository,
            BCryptPasswordEncoder passwordEncoder,
            JwtService jwtService,
            FreelancerTaskRepository freelancerTaskRepository,
            CloudinaryService cloudinaryService
    ) {
        this.freelancerRepository = freelancerRepository;
        this.techStackRepository = techStackRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.freelancerTaskRepository = freelancerTaskRepository;
        this.cloudinaryService = cloudinaryService;
    }

    // =========================================================
    // EXTRACT EMAIL FROM JWT
    // =========================================================

    public String extractEmail(String authHeader) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            throw new RuntimeException("Token Required");
        }

        String token = authHeader.substring(7);

        return jwtService.extractUsername(token);
    }

    // =========================================================
    // CREATE FREELANCER
    // =========================================================

    @Override
    public FreelancerResponseDTO create(
            FreelancerRequestDTO dto,
            MultipartFile profile,
            MultipartFile aadhaar,
            MultipartFile resume
    ) {

        Freelancer freelancer = new Freelancer();

        // Map normal fields
        mapDtoToEntity(dto, freelancer);

        // Password
        if (dto.getPassword() != null &&
                !dto.getPassword().isBlank()) {

            PasswordPolicy.validate(dto.getPassword());
            freelancer.setPassword(
                    passwordEncoder.encode(dto.getPassword())
            );

        } else {

            freelancer.setPassword(
                    passwordEncoder.encode(PasswordPolicy.generateTemporaryPassword())
            );
        }

        // =====================================================
        // PROFILE UPLOAD
        // =====================================================

        if (profile != null && !profile.isEmpty()) {

            String profileUrl =
                    cloudinaryService.uploadFile(
                            profile,
                            "freelancer/profile"
                    );

            freelancer.setProfile(profileUrl);
        }

        // =====================================================
        // AADHAAR UPLOAD
        // =====================================================

        if (aadhaar != null && !aadhaar.isEmpty()) {

            String aadhaarUrl =
                    cloudinaryService.uploadFile(
                            aadhaar,
                            "freelancer/aadhaar"
                    );

            freelancer.setAadhaar(aadhaarUrl);
        }

        // =====================================================
        // RESUME UPLOAD
        // =====================================================

        if (resume != null && !resume.isEmpty()) {

            String resumeUrl =
                    cloudinaryService.uploadFile(
                            resume,
                            "freelancer/resume"
                    );

            freelancer.setResume(resumeUrl);
        }

        Freelancer saved =
                freelancerRepository.save(freelancer);

        return mapEntityToDto(saved);
    }

    // =========================================================
    // UPDATE FREELANCER
    // =========================================================

    @Override
    public FreelancerResponseDTO update(
            Long id,
            FreelancerRequestDTO dto,
            MultipartFile profile,
            MultipartFile aadhaar,
            MultipartFile resume
    ) {

        Freelancer freelancer =
                freelancerRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Freelancer not found: " + id
                                )
                        );

        // Update normal fields
        mapDtoToEntity(dto, freelancer);

        // =====================================================
        // UPDATE PROFILE ONLY IF NEW FILE IS PROVIDED
        // =====================================================

        if (profile != null && !profile.isEmpty()) {

            String profileUrl =
                    cloudinaryService.uploadFile(
                            profile,
                            "freelancer/profile"
                    );

            freelancer.setProfile(profileUrl);
        }

        // =====================================================
        // UPDATE AADHAAR ONLY IF NEW FILE IS PROVIDED
        // =====================================================

        if (aadhaar != null && !aadhaar.isEmpty()) {

            String aadhaarUrl =
                    cloudinaryService.uploadFile(
                            aadhaar,
                            "freelancer/aadhaar"
                    );

            freelancer.setAadhaar(aadhaarUrl);
        }

        // =====================================================
        // UPDATE RESUME ONLY IF NEW FILE IS PROVIDED
        // =====================================================

        if (resume != null && !resume.isEmpty()) {

            String resumeUrl =
                    cloudinaryService.uploadFile(
                            resume,
                            "freelancer/resume"
                    );

            freelancer.setResume(resumeUrl);
        }

        Freelancer updated =
                freelancerRepository.save(freelancer);

        return mapEntityToDto(updated);
    }

    // =========================================================
    // FREELANCER LOGIN
    // =========================================================

    @Override
    public Map<String, String> loginFreelancer(
            LoginRequestDto request
    ) {

        Freelancer freelancer =
                freelancerRepository.findByEmail(request.getEmail())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Freelancer not found"
                                )
                        );

        if (!Boolean.TRUE.equals(freelancer.getActive())) {
            throw new IllegalStateException("Your account is inactive");
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                freelancer.getPassword()
        )) {

            throw new InvalidCredentialsException("Invalid email or password");
        }

        UserDetails userDetails =
                User.builder()
                        .username(freelancer.getEmail())
                        .password(freelancer.getPassword())
                        .roles("FREELANCER")
                        .build();

        String token =
                jwtService.generateToken(userDetails);

        return Map.of(
                "freelancerId",
                freelancer.getId().toString(),

                "email",
                freelancer.getEmail(),

                "name",
                freelancer.getName(),

                "token",
                token,

                "role",
                "FREELANCER"
        );
    }

    // =========================================================
    // MY TASKS
    // =========================================================

    @Override
    public List<FreelancerTaskResponseDTO> getMyTasks(
            String authHeader
    ) {

        String email = extractEmail(authHeader);

        Freelancer freelancer =
                freelancerRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Freelancer not found"
                                )
                        );

        List<FreelancerTask> tasks =
                freelancerTaskRepository.findByFreelancerId(
                        freelancer.getId()
                );

        return tasks.stream()
                .map(this::mapTaskEntityToDto)
                .collect(Collectors.toList());
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    @Override
    public FreelancerResponseDTO getById(Long id) {

        Freelancer freelancer =
                freelancerRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Freelancer not found: " + id
                                )
                        );

        return mapEntityToDto(freelancer);
    }

    // =========================================================
    // GET ALL
    // =========================================================

    @Override
    public Page<FreelancerResponseDTO> getAll(Pageable pageable) {
        return freelancerRepository.findAll(pageable).map(this::mapEntityToDto);
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Override
    public void delete(Long id) {

        freelancerRepository.deleteById(id);
    }

    // =========================================================
    // FILTER BY DISTRICT
    // =========================================================

    @Override
    public List<FreelancerResponseDTO> filterByDistrict(
            String district
    ) {

        return freelancerRepository
                .findByDistrictIgnoreCase(district)
                .stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    // =========================================================
    // FILTER BY TECH STACK
    // =========================================================

    @Override
    public List<FreelancerResponseDTO> filterByTechStack(
            String techStackName
    ) {

        return freelancerRepository
                .findByTechStackName(techStackName)
                .stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    // =========================================================
    // MAP DTO TO ENTITY
    // =========================================================

    private void mapDtoToEntity(
            FreelancerRequestDTO dto,
            Freelancer freelancer
    ) {

        freelancer.setName(dto.getName());

        freelancer.setYearOfPassing(
                dto.getYearOfPassing()
        );

        freelancer.setExperience(
                dto.getExperience()
        );

        freelancer.setDistrict(
                dto.getDistrict()
        );

        freelancer.setAddress(
                dto.getAddress()
        );

        freelancer.setMobileNo(
                dto.getMobileNo()
        );

        freelancer.setEmail(
                dto.getEmail()
        );

        // DO NOT SET:
        // resume
        // aadhaar
        // profile
        //
        // They are handled through MultipartFile
        // and uploaded to Cloudinary.

        List<TechStack> techStacks =
                dto.getTechStackNames()
                        .stream()
                        .map(name ->
                                techStackRepository
                                        .findByNameIgnoreCase(name)
                                        .orElseGet(() ->
                                                techStackRepository.save(
                                                        new TechStack(name)
                                                )
                                        )
                        )
                        .collect(Collectors.toList());

        freelancer.setTechStacks(techStacks);
    }

    // =========================================================
    // MAP ENTITY TO RESPONSE DTO
    // =========================================================

    private FreelancerResponseDTO mapEntityToDto(
            Freelancer freelancer
    ) {

        FreelancerResponseDTO dto =
                new FreelancerResponseDTO();

        dto.setId(freelancer.getId());

        dto.setName(freelancer.getName());

        dto.setYearOfPassing(
                freelancer.getYearOfPassing()
        );

        dto.setExperience(
                freelancer.getExperience()
        );

        dto.setDistrict(
                freelancer.getDistrict()
        );

        dto.setAddress(
                freelancer.getAddress()
        );

        dto.setMobileNo(
                freelancer.getMobileNo()
        );

        dto.setEmail(
                freelancer.getEmail()
        );

        // File URLs
        dto.setProfile(
                freelancer.getProfile()
        );

        dto.setResume(
                freelancer.getResume()
        );

        dto.setAadhaar(
                freelancer.getAadhaar()
        );

        dto.setTechStackNames(
                freelancer.getTechStacks()
                        .stream()
                        .map(TechStack::getName)
                        .collect(Collectors.toList())
        );

        return dto;
    }

    // =========================================================
    // MAP TASK ENTITY TO DTO
    // =========================================================

    private FreelancerTaskResponseDTO mapTaskEntityToDto(
            FreelancerTask task
    ) {

        FreelancerTaskResponseDTO dto =
                new FreelancerTaskResponseDTO();

        dto.setId(task.getId());

        dto.setOrgName(task.getOrgName());

        dto.setNoOfDays(task.getNoOfDays());

        dto.setStartDate(task.getStartDate());

        dto.setEndDate(task.getEndDate());

        dto.setMeetingLink(task.getMeetingLink());

        dto.setMeetingEmail(task.getMeetingEmail());

        dto.setMeetingPassword(
                task.getMeetingPassword()
        );

        dto.setDepartment(task.getDepartment());

        dto.setDomain(task.getDomain());

        dto.setNoOfStudents(
                task.getNoOfStudents()
        );

        dto.setSyllabus(task.getSyllabus());

        dto.setStatus(task.getStatus());

        dto.setFreelancerIds(
                task.getFreelancers()
                        .stream()
                        .map(Freelancer::getId)
                        .collect(Collectors.toList())
        );

        dto.setFreelancerNames(
                task.getFreelancers()
                        .stream()
                        .map(Freelancer::getName)
                        .collect(Collectors.toList())
        );

        return dto;
    }
}
