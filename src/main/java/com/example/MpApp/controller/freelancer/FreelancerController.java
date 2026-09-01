package com.example.MpApp.controller.freelancer;

import com.example.MpApp.dto.Freelancer.FreelancerRequestDTO;
import com.example.MpApp.dto.Freelancer.FreelancerResponseDTO;
import com.example.MpApp.dto.Freelancer.FreelancerTaskResponseDTO;
import com.example.MpApp.dto.Freelancer.LoginRequestDto;
import com.example.MpApp.service.freelancer.FreelancerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FreelancerController {

    private final FreelancerService freelancerService;

    public FreelancerController(FreelancerService freelancerService) {
        this.freelancerService = freelancerService;
    }

    @PostMapping("/freelancer/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequestDto request) {
        Map<String, String> response = freelancerService.loginFreelancer(request);

        if (response.containsKey("message")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/freelancer/mytasks")
    public ResponseEntity<List<FreelancerTaskResponseDTO>> getMyTasks(
            @RequestHeader("Authorization") String authHeader
    ) {
        return ResponseEntity.ok(
                freelancerService.getMyTasks(authHeader)
        );
    }

    @PostMapping("/admin/freelancers/create")
    public ResponseEntity<FreelancerResponseDTO> create(@RequestBody FreelancerRequestDTO dto) {
        return ResponseEntity.ok(freelancerService.create(dto));
    }

    @PutMapping("/admin/freelancers/update/{id}")
    public ResponseEntity<FreelancerResponseDTO> update(@PathVariable Long id,
                                                        @RequestBody FreelancerRequestDTO dto) {
        return ResponseEntity.ok(freelancerService.update(id, dto));
    }

    @GetMapping("/admin/freelancers/{id}")
    public ResponseEntity<FreelancerResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(freelancerService.getById(id));
    }

    @GetMapping("/admin/freelancers/get-all")
    public ResponseEntity<List<FreelancerResponseDTO>> getAll() {
        return ResponseEntity.ok(freelancerService.getAll());
    }

    @DeleteMapping("/admin/freelancers/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        freelancerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/freelancers/filter/district/{district}")
    public ResponseEntity<List<FreelancerResponseDTO>> filterByDistrict(@PathVariable String district) {
        return ResponseEntity.ok(freelancerService.filterByDistrict(district));
    }

    @GetMapping("/admin/freelancers/filter/techstack/{techStackName}")
    public ResponseEntity<List<FreelancerResponseDTO>> filterByTechStack(@PathVariable String techStackName) {
        return ResponseEntity.ok(freelancerService.filterByTechStack(techStackName));
    }
}