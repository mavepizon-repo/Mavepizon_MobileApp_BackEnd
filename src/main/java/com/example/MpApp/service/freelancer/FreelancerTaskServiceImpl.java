package com.example.MpApp.service.freelancer;

import com.example.MpApp.dto.Freelancer.FreelancerTaskRequestDTO;
import com.example.MpApp.dto.Freelancer.FreelancerTaskResponseDTO;
import com.example.MpApp.entity.freelancer.Freelancer;
import com.example.MpApp.entity.freelancer.FreelancerTask;
import com.example.MpApp.repository.freelancer.FreelancerRepository;
import com.example.MpApp.repository.freelancer.FreelancerTaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FreelancerTaskServiceImpl implements FreelancerTaskService {

    private final FreelancerTaskRepository freelancerTaskRepository;
    private final FreelancerRepository freelancerRepository;

    public FreelancerTaskServiceImpl(FreelancerTaskRepository freelancerTaskRepository,
                                     FreelancerRepository freelancerRepository) {
        this.freelancerTaskRepository = freelancerTaskRepository;
        this.freelancerRepository = freelancerRepository;
    }

    @Override
    public FreelancerTaskResponseDTO create(FreelancerTaskRequestDTO dto) {
        FreelancerTask task = new FreelancerTask();
        mapDtoToEntity(dto, task);
        FreelancerTask saved = freelancerTaskRepository.save(task);
        return mapEntityToDto(saved);
    }

    @Override
    public FreelancerTaskResponseDTO update(Long id, FreelancerTaskRequestDTO dto) {
        FreelancerTask task = freelancerTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FreelancerTask not found: " + id));
        mapDtoToEntity(dto, task);
        FreelancerTask updated = freelancerTaskRepository.save(task);
        return mapEntityToDto(updated);
    }

    @Override
    public FreelancerTaskResponseDTO getById(Long id) {
        FreelancerTask task = freelancerTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FreelancerTask not found: " + id));
        return mapEntityToDto(task);
    }

    @Override
    public List<FreelancerTaskResponseDTO> getAll() {
        return freelancerTaskRepository.findAll().stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
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

    private void mapDtoToEntity(FreelancerTaskRequestDTO dto, FreelancerTask task) {
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
        task.setSyllabus(dto.getSyllabus());
        task.setStatus(dto.getStatus());

        List<Freelancer> freelancers = freelancerRepository.findAllById(dto.getFreelancerIds());
        task.setFreelancers(freelancers);
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