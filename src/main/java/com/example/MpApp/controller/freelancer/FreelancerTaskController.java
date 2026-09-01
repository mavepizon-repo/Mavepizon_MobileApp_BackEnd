package com.example.MpApp.controller.freelancer;

import com.example.MpApp.dto.Freelancer.FreelancerTaskRequestDTO;
import com.example.MpApp.dto.Freelancer.FreelancerTaskResponseDTO;
import com.example.MpApp.service.freelancer.FreelancerTaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/freelancer-tasks")
public class FreelancerTaskController {

    private final FreelancerTaskService freelancerTaskService;

    public FreelancerTaskController(FreelancerTaskService freelancerTaskService) {
        this.freelancerTaskService = freelancerTaskService;
    }

    @PostMapping("/create")
    public ResponseEntity<FreelancerTaskResponseDTO> create(@RequestBody FreelancerTaskRequestDTO dto) {
        return ResponseEntity.ok(freelancerTaskService.create(dto));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<FreelancerTaskResponseDTO> update(@PathVariable Long id,
                                                            @RequestBody FreelancerTaskRequestDTO dto) {
        return ResponseEntity.ok(freelancerTaskService.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FreelancerTaskResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(freelancerTaskService.getById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<FreelancerTaskResponseDTO>> getAll() {
        return ResponseEntity.ok(freelancerTaskService.getAll());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        freelancerTaskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/freelancer/{freelancerId}")
    public ResponseEntity<List<FreelancerTaskResponseDTO>> getByFreelancerId(@PathVariable Long freelancerId) {
        return ResponseEntity.ok(freelancerTaskService.getByFreelancerId(freelancerId));
    }
}