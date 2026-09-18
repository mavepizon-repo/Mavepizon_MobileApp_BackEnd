package com.example.MpApp.controller.freelancer;

import com.example.MpApp.dto.Freelancer.FreelancerTaskRequestDTO;
import com.example.MpApp.dto.Freelancer.FreelancerTaskResponseDTO;
import com.example.MpApp.service.freelancer.FreelancerTaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/freelancer-tasks")
public class FreelancerTaskController {

    private final FreelancerTaskService freelancerTaskService;

    public FreelancerTaskController(FreelancerTaskService freelancerTaskService) {
        this.freelancerTaskService = freelancerTaskService;
    }

    @PostMapping(
            value = "/create",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<FreelancerTaskResponseDTO> create(

            @RequestPart("data")
            FreelancerTaskRequestDTO dto,

            @RequestPart(value = "syllabus", required = false)
            MultipartFile syllabus
    ) {

        return ResponseEntity.ok(
                freelancerTaskService.create(
                        dto,
                        syllabus
                )
        );
    }

    @PutMapping(
            value = "/update/{id}",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<FreelancerTaskResponseDTO> update(

            @PathVariable Long id,

            @RequestPart("data")
            FreelancerTaskRequestDTO dto,

            @RequestPart(value = "syllabus", required = false)
            MultipartFile syllabus
    ) {

        return ResponseEntity.ok(
                freelancerTaskService.update(
                        id,
                        dto,
                        syllabus
                )
        );
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