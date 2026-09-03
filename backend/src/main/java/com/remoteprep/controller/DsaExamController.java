package com.remoteprep.controller;

import com.remoteprep.dto.GenerateDsaExamRequest;
import com.remoteprep.dto.GenerateDsaExamResponse;
import com.remoteprep.service.DsaQuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST Controller for DSA Exam Generation.
 */
@RestController
@RequestMapping("/api/dsa")
public class DsaExamController {

    private final DsaQuestionService dsaQuestionService;

    public DsaExamController(DsaQuestionService dsaQuestionService) {
        this.dsaQuestionService = dsaQuestionService;
    }

    /**
     * Generates or retrieves the 2 assigned DSA problems (1 EASY + 1 MEDIUM) for an active assessment.
     * POST /api/dsa/exam
     */
    @PostMapping("/exam")
    public ResponseEntity<?> generateDsaExam(@RequestBody(required = false) GenerateDsaExamRequest request) {
        try {
            GenerateDsaExamResponse response = dsaQuestionService.generateDsaExam(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
