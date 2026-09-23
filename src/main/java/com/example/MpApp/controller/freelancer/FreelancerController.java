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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(
            value = "/admin/freelancers/create",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<FreelancerResponseDTO> create(
            @Valid @RequestPart("data") FreelancerRequestDTO dto,

            @RequestPart(value = "profile", required = false)
            MultipartFile profile,

            @RequestPart(value = "aadhaar", required = false)
            MultipartFile aadhaar,

            @RequestPart(value = "resume", required = false)
            MultipartFile resume
    ) {

        return ResponseEntity.ok(
                freelancerService.create(
                        dto,
                        profile,
                        aadhaar,
                        resume
                )
        );
    }


    @PutMapping(
            value = "/admin/freelancers/update/{id}",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<FreelancerResponseDTO> update(
            @PathVariable Long id,

            @Valid @RequestPart("data") FreelancerRequestDTO dto,

            @RequestPart(value = "profile", required = false)
            MultipartFile profile,

            @RequestPart(value = "aadhaar", required = false)
            MultipartFile aadhaar,

            @RequestPart(value = "resume", required = false)
            MultipartFile resume
    ) {

        return ResponseEntity.ok(
                freelancerService.update(
                        id,
                        dto,
                        profile,
                        aadhaar,
                        resume
                )
        );
    }

    @GetMapping("/admin/freelancers/{id}")
    public ResponseEntity<FreelancerResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(freelancerService.getById(id));
    }

    @GetMapping("/admin/freelancers/get-all")
    public ResponseEntity<Page<FreelancerResponseDTO>> getAll(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(freelancerService.getAll(pageable));
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