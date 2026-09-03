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
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
