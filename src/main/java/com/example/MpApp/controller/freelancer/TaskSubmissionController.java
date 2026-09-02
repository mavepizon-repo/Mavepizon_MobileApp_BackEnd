package com.example.MpApp.controller.freelancer;

import com.example.MpApp.dto.Freelancer.TaskSubmissionRequestDTO;
import com.example.MpApp.dto.Freelancer.TaskSubmissionResponseDTO;
import com.example.MpApp.service.freelancer.TaskSubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/freelancer/task-submissions")
public class TaskSubmissionController {

    private final TaskSubmissionService taskSubmissionService;

    public TaskSubmissionController(TaskSubmissionService taskSubmissionService) {
        this.taskSubmissionService = taskSubmissionService;
    }

    @PostMapping("/submit")
    public ResponseEntity<TaskSubmissionResponseDTO> create(@RequestBody TaskSubmissionRequestDTO dto) {
        return ResponseEntity.ok(taskSubmissionService.create(dto));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<TaskSubmissionResponseDTO> update(@PathVariable Long id,
                                                            @RequestBody TaskSubmissionRequestDTO dto) {
        return ResponseEntity.ok(taskSubmissionService.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskSubmissionResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(taskSubmissionService.getById(id));
    }

    @GetMapping("/task/{freelancerTaskId}")
    public ResponseEntity<TaskSubmissionResponseDTO> getByFreelancerTaskId(@PathVariable Long freelancerTaskId) {
        return ResponseEntity.ok(taskSubmissionService.getByFreelancerTaskId(freelancerTaskId));
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<TaskSubmissionResponseDTO>> getAll() {
        return ResponseEntity.ok(taskSubmissionService.getAll());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskSubmissionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}