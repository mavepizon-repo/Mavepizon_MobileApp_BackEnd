package com.example.MpApp.service.freelancer;

import com.example.MpApp.dto.Freelancer.TaskSubmissionRequestDTO;
import com.example.MpApp.dto.Freelancer.TaskSubmissionResponseDTO;
import com.example.MpApp.entity.freelancer.FreelancerTask;
import com.example.MpApp.entity.freelancer.TaskSubmission;
import com.example.MpApp.repository.freelancer.FreelancerTaskRepository;
import com.example.MpApp.repository.freelancer.TaskSubmissionRepository;
import com.example.MpApp.service.CloudinaryService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskSubmissionService {

    private final TaskSubmissionRepository taskSubmissionRepository;
    private final FreelancerTaskRepository freelancerTaskRepository;
    private final CloudinaryService cloudinaryService;

    public TaskSubmissionService(
            TaskSubmissionRepository taskSubmissionRepository,
            FreelancerTaskRepository freelancerTaskRepository,
            CloudinaryService cloudinaryService
    ) {
        this.taskSubmissionRepository = taskSubmissionRepository;
        this.freelancerTaskRepository = freelancerTaskRepository;
        this.cloudinaryService = cloudinaryService;
    }

    // =========================
    // CREATE
    // =========================
    public TaskSubmissionResponseDTO create(
            TaskSubmissionRequestDTO dto,
            MultipartFile notes
    ) {

        FreelancerTask freelancerTask =
                freelancerTaskRepository.findById(
                        dto.getFreelancerTaskId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "FreelancerTask not found: "
                                        + dto.getFreelancerTaskId()
                        )
                );

        // Only one submission allowed for one task
        if (taskSubmissionRepository
                .findByFreelancerTaskId(
                        dto.getFreelancerTaskId()
                )
                .isPresent()) {

            throw new RuntimeException(
                    "Submission already exists for task: "
                            + dto.getFreelancerTaskId()
            );
        }

        TaskSubmission submission =
                new TaskSubmission();

        submission.setFreelancerTask(freelancerTask);
        submission.setStatus(dto.getStatus());
        submission.setFeedback(dto.getFeedback());

        // =========================
        // Upload Notes to Cloudinary
        // =========================
        if (notes != null && !notes.isEmpty()) {

            String notesUrl =
                    cloudinaryService.uploadFile(
                            notes,
                            "freelancer/notes"
                    );

            submission.setNotes(notesUrl);
        }

        TaskSubmission saved =
                taskSubmissionRepository.save(submission);

        // Update parent FreelancerTask status
        freelancerTask.setStatus(dto.getStatus());

        freelancerTaskRepository.save(freelancerTask);

        return mapEntityToDto(saved);
    }

    // =========================
    // UPDATE
    // =========================
    public TaskSubmissionResponseDTO update(
            Long id,
            TaskSubmissionRequestDTO dto,
            MultipartFile notes
    ) {

        TaskSubmission submission =
                taskSubmissionRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "TaskSubmission not found: "
                                                + id
                                )
                        );

        submission.setStatus(dto.getStatus());
        submission.setFeedback(dto.getFeedback());

        // =========================
        // Replace Notes File
        // =========================
        // Only replace if a new file is provided
        if (notes != null && !notes.isEmpty()) {

            String notesUrl =
                    cloudinaryService.uploadFile(
                            notes,
                            "freelancer/notes"
                    );

            submission.setNotes(notesUrl);
        }

        TaskSubmission updated =
                taskSubmissionRepository.save(submission);

        // Update parent FreelancerTask status
        FreelancerTask freelancerTask =
                submission.getFreelancerTask();

        freelancerTask.setStatus(dto.getStatus());

        freelancerTaskRepository.save(freelancerTask);

        return mapEntityToDto(updated);
    }

    // =========================
    // GET BY ID
    // =========================
    public TaskSubmissionResponseDTO getById(Long id) {

        TaskSubmission submission =
                taskSubmissionRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "TaskSubmission not found: "
                                                + id
                                )
                        );

        return mapEntityToDto(submission);
    }

    // =========================
    // GET BY FREELANCER TASK ID
    // =========================
    public TaskSubmissionResponseDTO getByFreelancerTaskId(
            Long freelancerTaskId
    ) {

        TaskSubmission submission =
                taskSubmissionRepository
                        .findByFreelancerTaskId(
                                freelancerTaskId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No submission for task: "
                                                + freelancerTaskId
                                )
                        );

        return mapEntityToDto(submission);
    }

    // =========================
    // GET ALL
    // =========================
    public List<TaskSubmissionResponseDTO> getAll() {

        return taskSubmissionRepository.findAll()
                .stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    // =========================
    // DELETE
    // =========================
    public void delete(Long id) {

        TaskSubmission submission =
                taskSubmissionRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "TaskSubmission not found: "
                                                + id
                                )
                        );

        taskSubmissionRepository.delete(submission);
    }

    // =========================
    // ENTITY → DTO
    // =========================
    private TaskSubmissionResponseDTO mapEntityToDto(
            TaskSubmission submission
    ) {

        TaskSubmissionResponseDTO dto =
                new TaskSubmissionResponseDTO();

        dto.setId(submission.getId());

        dto.setFreelancerTaskId(
                submission.getFreelancerTask().getId()
        );

        dto.setStatus(submission.getStatus());

        // Cloudinary Notes URL
        dto.setNotes(submission.getNotes());

        dto.setFeedback(submission.getFeedback());

        return dto;
    }
}