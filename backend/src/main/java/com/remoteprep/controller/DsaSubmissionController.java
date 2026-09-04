package com.remoteprep.controller;

import com.remoteprep.dto.SubmitDsaCodeRequest;
import com.remoteprep.dto.SubmitDsaCodeResponse;
import com.remoteprep.service.DsaSubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST Controller for candidate DSA code submissions.
 */
@RestController
@RequestMapping("/api/dsa")
public class DsaSubmissionController {

    private final DsaSubmissionService dsaSubmissionService;

    public DsaSubmissionController(DsaSubmissionService dsaSubmissionService) {
        this.dsaSubmissionService = dsaSubmissionService;
    }

    /**
     * Submits candidate code for an assigned DSA question.
     * POST /api/dsa/submit
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitCode(@RequestBody(required = false) SubmitDsaCodeRequest request) {
        try {
            SubmitDsaCodeResponse response = dsaSubmissionService.submitCode(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg != null && (msg.contains("not found") || msg.contains("Not found"))) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", msg));
            }
            if (msg != null && msg.contains("not assigned to assessment")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", msg));
            }
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        } catch (IllegalStateException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("not in IN_PROGRESS state")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", msg));
            }
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred during submission evaluation"));
        }
    }
}
