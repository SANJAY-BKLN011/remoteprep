package com.remoteprep.controller;

import com.remoteprep.dto.AssessmentResultResponse;
import com.remoteprep.dto.CompleteAssessmentResponse;
import com.remoteprep.service.AssessmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST Controller for Assessment lifecycle operations.
 * Exposes endpoint to complete assessment and compute final scores.
 */
@RestController
@RequestMapping("/api/assessment")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    /**
     * Completes an assessment attempt, calculates scores, and returns final results.
     * POST /api/assessment/{assessmentId}/complete
     */
    @PostMapping("/{assessmentId}/complete")
    public ResponseEntity<?> completeAssessment(@PathVariable Long assessmentId) {
        try {
            CompleteAssessmentResponse response = assessmentService.completeAssessment(assessmentId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg != null && (msg.contains("not found") || msg.contains("Not found"))) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", msg));
            }
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        } catch (IllegalStateException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("no student")) {
                return ResponseEntity.badRequest().body(Map.of("error", msg));
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", msg));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred during assessment completion"));
        }
    }

    /**
     * Retrieves the authoritative final result of a completed assessment.
     * GET /api/assessment/{assessmentId}/result
     */
    @GetMapping("/{assessmentId}/result")
    public ResponseEntity<?> getAssessmentResult(@PathVariable Long assessmentId) {
        try {
            AssessmentResultResponse response = assessmentService.getAssessmentResult(assessmentId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg != null && (msg.contains("not found") || msg.contains("Not found"))) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", msg));
            }
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        } catch (IllegalStateException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("no student")) {
                return ResponseEntity.badRequest().body(Map.of("error", msg));
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", msg));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred while retrieving the assessment result"));
        }
    }
}
