package com.example.MpApp.controller.freelancer;

import com.example.MpApp.dto.Freelancer.FreelancerRequestDTO;
import com.example.MpApp.dto.Freelancer.FreelancerResponseDTO;
import com.example.MpApp.service.freelancer.FreelancerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/freelancers")
public class FreelancerController {

    private final FreelancerService freelancerService;

    public FreelancerController(FreelancerService freelancerService) {
        this.freelancerService = freelancerService;
    }

    @PostMapping("/create")
    public ResponseEntity<FreelancerResponseDTO> create(@RequestBody FreelancerRequestDTO dto) {
        return ResponseEntity.ok(freelancerService.create(dto));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<FreelancerResponseDTO> update(@PathVariable Long id,
                                                        @RequestBody FreelancerRequestDTO dto) {
        return ResponseEntity.ok(freelancerService.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FreelancerResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(freelancerService.getById(id));
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<FreelancerResponseDTO>> getAll() {
        return ResponseEntity.ok(freelancerService.getAll());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        freelancerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter/district/{district}")
    public ResponseEntity<List<FreelancerResponseDTO>> filterByDistrict(@PathVariable String district) {
        return ResponseEntity.ok(freelancerService.filterByDistrict(district));
    }

    @GetMapping("/filter/techstack/{techStackName}")
    public ResponseEntity<List<FreelancerResponseDTO>> filterByTechStack(@PathVariable String techStackName) {
        return ResponseEntity.ok(freelancerService.filterByTechStack(techStackName));
    }
}