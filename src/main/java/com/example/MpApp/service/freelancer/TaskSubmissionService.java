package com.example.MpApp.service.freelancer;

import com.example.MpApp.dto.Freelancer.TaskSubmissionRequestDTO;
import com.example.MpApp.dto.Freelancer.TaskSubmissionResponseDTO;
import com.example.MpApp.entity.freelancer.FreelancerTask;
import com.example.MpApp.entity.freelancer.TaskSubmission;
import com.example.MpApp.repository.freelancer.FreelancerTaskRepository;
import com.example.MpApp.repository.freelancer.TaskSubmissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskSubmissionService {

    private final TaskSubmissionRepository taskSubmissionRepository;
    private final FreelancerTaskRepository freelancerTaskRepository;

    public TaskSubmissionService(TaskSubmissionRepository taskSubmissionRepository,
                                 FreelancerTaskRepository freelancerTaskRepository) {
        this.taskSubmissionRepository = taskSubmissionRepository;
        this.freelancerTaskRepository = freelancerTaskRepository;
    }

    public TaskSubmissionResponseDTO create(TaskSubmissionRequestDTO dto) {
        FreelancerTask freelancerTask = freelancerTaskRepository.findById(dto.getFreelancerTaskId())
                .orElseThrow(() -> new RuntimeException("FreelancerTask not found: " + dto.getFreelancerTaskId()));

        if (taskSubmissionRepository.findByFreelancerTaskId(dto.getFreelancerTaskId()).isPresent()) {
            throw new RuntimeException("Submission already exists for task: " + dto.getFreelancerTaskId());
        }

        TaskSubmission submission = new TaskSubmission();
        submission.setFreelancerTask(freelancerTask);
        submission.setStatus(dto.getStatus());
        submission.setNotes(dto.getNotes());
        submission.setFeedback(dto.getFeedback());
        TaskSubmission saved = taskSubmissionRepository.save(submission);

        // push status back onto the parent task
        freelancerTask.setStatus(dto.getStatus());
        freelancerTaskRepository.save(freelancerTask);

        return mapEntityToDto(saved);
    }

    public TaskSubmissionResponseDTO update(Long id, TaskSubmissionRequestDTO dto) {
        TaskSubmission submission = taskSubmissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TaskSubmission not found: " + id));

        submission.setStatus(dto.getStatus());
        submission.setNotes(dto.getNotes());
        submission.setFeedback(dto.getFeedback());
        TaskSubmission updated = taskSubmissionRepository.save(submission);

        FreelancerTask freelancerTask = submission.getFreelancerTask();
        freelancerTask.setStatus(dto.getStatus());
        freelancerTaskRepository.save(freelancerTask);

        return mapEntityToDto(updated);
    }

    public TaskSubmissionResponseDTO getById(Long id) {
        TaskSubmission submission = taskSubmissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TaskSubmission not found: " + id));
        return mapEntityToDto(submission);
    }

    public TaskSubmissionResponseDTO getByFreelancerTaskId(Long freelancerTaskId) {
        TaskSubmission submission = taskSubmissionRepository.findByFreelancerTaskId(freelancerTaskId)
                .orElseThrow(() -> new RuntimeException("No submission for task: " + freelancerTaskId));
        return mapEntityToDto(submission);
    }

    public List<TaskSubmissionResponseDTO> getAll() {
        return taskSubmissionRepository.findAll().stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    public void delete(Long id) {
        taskSubmissionRepository.deleteById(id);
    }

    private TaskSubmissionResponseDTO mapEntityToDto(TaskSubmission submission) {
        TaskSubmissionResponseDTO dto = new TaskSubmissionResponseDTO();
        dto.setId(submission.getId());
        dto.setFreelancerTaskId(submission.getFreelancerTask().getId());
        dto.setStatus(submission.getStatus());
        dto.setNotes(submission.getNotes());
        dto.setFeedback(submission.getFeedback());
        return dto;
    }
}