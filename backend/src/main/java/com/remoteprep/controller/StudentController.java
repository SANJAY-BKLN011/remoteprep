package com.remoteprep.controller;

import com.remoteprep.dto.StartAssessmentRequest;
import com.remoteprep.dto.StartAssessmentResponse;
import com.remoteprep.service.AssessmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST Controller for Student and Assessment initialization endpoints.
 */
@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final AssessmentService assessmentService;

    public StudentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    /**
     * Starts an assessment attempt for a student.
     * POST /api/students/start
     * 
     * Validates that both name and rollNumber are non-blank.
     * Reuses existing student record for identical roll numbers, but always generates
     * a fresh Assessment attempt.
     */
    @PostMapping("/start")
    public ResponseEntity<?> startAssessment(@RequestBody(required = false) StartAssessmentRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Request body is required"));
        }

        String name = request.getName();
        String rollNumber = request.getRollNumber();

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Student name must not be blank"));
        }

        if (rollNumber == null || rollNumber.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Student roll number must not be blank"));
        }

        StartAssessmentResponse response = assessmentService.startAssessment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
