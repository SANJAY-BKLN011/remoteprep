package com.remoteprep.controller;

import com.remoteprep.dto.DsaRunRequest;
import com.remoteprep.dto.DsaRunResponse;
import com.remoteprep.service.DsaRunService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST Controller for executing candidate DSA code against demo test cases (Run API).
 * POST /api/dsa/run
 */
@RestController
@RequestMapping("/api/dsa")
public class DsaRunController {

    private final DsaRunService dsaRunService;

    public DsaRunController(DsaRunService dsaRunService) {
        this.dsaRunService = dsaRunService;
    }

    /**
     * Executes candidate source code against exactly two demo test cases for an assigned DSA question.
     * Temporary execution only: does not persist submissions or modify assessment score.
     */
    @PostMapping("/run")
    public ResponseEntity<?> runCode(@RequestBody(required = false) DsaRunRequest request) {
        try {
            DsaRunResponse response = dsaRunService.runCode(request);
            return ResponseEntity.ok(response);
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
                    .body(Map.of("error", "An unexpected error occurred during execution"));
        }
    }
}
