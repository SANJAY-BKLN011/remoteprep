package com.remoteprep.controller;

import com.remoteprep.entity.AptitudeTopic;
import com.remoteprep.service.AptitudeTopicService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller exposing Aptitude Topics API endpoints.
 */
@RestController
@RequestMapping("/api/aptitude")
public class AptitudeTopicController {

    private final AptitudeTopicService aptitudeTopicService;

    public AptitudeTopicController(AptitudeTopicService aptitudeTopicService) {
        this.aptitudeTopicService = aptitudeTopicService;
    }

    /**
     * HTTP GET endpoint to retrieve all aptitude syllabus topics.
     * Accessible at: GET http://localhost:8080/api/aptitude/topics
     * 
     * @return JSON array of AptitudeTopic objects from MySQL
     */
    @GetMapping("/topics")
    public List<AptitudeTopic> getAllTopics() {
        return aptitudeTopicService.getAllTopics();
    }
}
