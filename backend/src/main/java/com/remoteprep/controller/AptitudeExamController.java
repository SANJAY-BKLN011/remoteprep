package com.remoteprep.controller;

import com.remoteprep.dto.GenerateExamRequest;
import com.remoteprep.dto.GenerateExamResponse;
import com.remoteprep.service.AptitudeQuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST Controller for Aptitude Exam generation.
 */
@RestController
@RequestMapping("/api/aptitude")
public class AptitudeExamController {

    private final AptitudeQuestionService aptitudeQuestionService;

    public AptitudeExamController(AptitudeQuestionService aptitudeQuestionService) {
        this.aptitudeQuestionService = aptitudeQuestionService;
    }

    /**
     * Generates a randomized 20-question aptitude examination for an active assessment attempt.
     * POST /api/aptitude/exam
     */
    @PostMapping("/exam")
    public ResponseEntity<?> generateExam(@RequestBody(required = false) GenerateExamRequest request) {
        try {
            GenerateExamResponse response = aptitudeQuestionService.generateExam(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
