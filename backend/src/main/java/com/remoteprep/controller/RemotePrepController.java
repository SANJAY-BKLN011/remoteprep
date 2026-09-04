package com.remoteprep.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class RemotePrepController {

    /**
     * Test and health check endpoint to verify that the Spring Boot backend server is active and responding.
     * Accessible at:
     *   GET http://localhost:8080/api/test
     *   GET http://localhost:8080/api/health
     */
    @GetMapping({"/test", "/health"})
    public Map<String, String> test() {
        return Map.of("status", "UP", "message", "RemotePrep backend is working");
    }

}
