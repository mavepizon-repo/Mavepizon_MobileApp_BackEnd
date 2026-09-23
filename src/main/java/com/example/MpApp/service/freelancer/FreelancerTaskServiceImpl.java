package com.example.MpApp.service.freelancer;

import com.example.MpApp.dto.Freelancer.FreelancerTaskRequestDTO;
import com.example.MpApp.dto.Freelancer.FreelancerTaskResponseDTO;
import com.example.MpApp.entity.freelancer.Freelancer;
import com.example.MpApp.entity.freelancer.FreelancerTask;
import com.example.MpApp.repository.freelancer.FreelancerRepository;
import com.example.MpApp.repository.freelancer.FreelancerTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;
import com.example.MpApp.service.CloudinaryService;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FreelancerTaskServiceImpl implements FreelancerTaskService {

    private final FreelancerTaskRepository freelancerTaskRepository;
    private final FreelancerRepository freelancerRepository;
    private final CloudinaryService cloudinaryService;

    public FreelancerTaskServiceImpl(
            FreelancerTaskRepository freelancerTaskRepository,
            FreelancerRepository freelancerRepository,
            CloudinaryService cloudinaryService
    ) {
        this.freelancerTaskRepository = freelancerTaskRepository;
        this.freelancerRepository = freelancerRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    public FreelancerTaskResponseDTO create(
            FreelancerTaskRequestDTO dto,
            MultipartFile syllabus
    ) {

        FreelancerTask task = new FreelancerTask();

        mapDtoToEntity(dto, task);

        // Upload syllabus to Cloudinary
        if (syllabus != null && !syllabus.isEmpty()) {

            String syllabusUrl =
                    cloudinaryService.uploadFile(
                            syllabus,
                            "freelancer/syllabus"
                    );

            task.setSyllabus(syllabusUrl);
        }

        FreelancerTask saved =
                freelancerTaskRepository.save(task);

        return mapEntityToDto(saved);
    }

    @Override
    public FreelancerTaskResponseDTO update(
            Long id,
            FreelancerTaskRequestDTO dto,
            MultipartFile syllabus
    ) {

        FreelancerTask task =
                freelancerTaskRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "FreelancerTask not found: " + id
                                )
                        );

        mapDtoToEntity(dto, task);

        // Replace syllabus only if a new file is provided
        if (syllabus != null && !syllabus.isEmpty()) {

            String syllabusUrl =
                    cloudinaryService.uploadFile(
                            syllabus,
                            "freelancer/syllabus"
                    );

            task.setSyllabus(syllabusUrl);
        }

        FreelancerTask updated =
                freelancerTaskRepository.save(task);

        return mapEntityToDto(updated);
    }

    @Override
    public FreelancerTaskResponseDTO getById(Long id) {
        FreelancerTask task = freelancerTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FreelancerTask not found: " + id));
        return mapEntityToDto(task);
    }

    @Override
    public Page<FreelancerTaskResponseDTO> getAll(Pageable pageable) {
        return freelancerTaskRepository.findAll(pageable).map(this::mapEntityToDto);
    }

    @Override
    public void delete(Long id) {
        freelancerTaskRepository.deleteById(id);
    }

    @Override
    public List<FreelancerTaskResponseDTO> getByFreelancerId(Long freelancerId) {
        return freelancerTaskRepository.findByFreelancerId(freelancerId).stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    private void mapDtoToEntity(
            FreelancerTaskRequestDTO dto,
            FreelancerTask task
    ) {
        task.setOrgName(dto.getOrgName());
        task.setNoOfDays(dto.getNoOfDays());
        task.setStartDate(dto.getStartDate());
        task.setEndDate(dto.getEndDate());
        task.setMeetingLink(dto.getMeetingLink());
        task.setMeetingEmail(dto.getMeetingEmail());
        task.setMeetingPassword(dto.getMeetingPassword());
        task.setDepartment(dto.getDepartment());
        task.setDomain(dto.getDomain());
        task.setNoOfStudents(dto.getNoOfStudents());
        task.setStatus(dto.getStatus());

        if (dto.getFreelancerIds() != null) {
            List<Freelancer> freelancers =
                    freelancerRepository.findAllById(dto.getFreelancerIds());

            task.setFreelancers(freelancers);
        }
    }

    private FreelancerTaskResponseDTO mapEntityToDto(FreelancerTask task) {
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