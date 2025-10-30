package com.shodh.backend.controller;

import com.shodh.backend.dto.SubmissionRequest;
import com.shodh.backend.dto.SubmissionResponse;
import com.shodh.backend.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:5173"}, allowCredentials = "true")
public class SubmissionController {
    private final SubmissionService submissionService;

    @PostMapping
    public ResponseEntity<SubmissionResponse> submitCode(@Valid @RequestBody SubmissionRequest request) {
        SubmissionResponse response = submissionService.submitCode(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{submissionId}")
    public ResponseEntity<SubmissionResponse> getSubmission(@PathVariable Long submissionId) {
        SubmissionResponse response = submissionService.getSubmissionById(submissionId);
        return ResponseEntity.ok(response);
    }
}
