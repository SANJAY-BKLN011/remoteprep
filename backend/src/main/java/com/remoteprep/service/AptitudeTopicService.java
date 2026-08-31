package com.remoteprep.service;

import com.remoteprep.entity.AptitudeTopic;
import com.remoteprep.repository.AptitudeTopicRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service Layer for Aptitude Topics.
 * Encapsulates business logic and acts as an intermediary between Controller and Repository.
 */
@Service
public class AptitudeTopicService {

    private final AptitudeTopicRepository aptitudeTopicRepository;

    public AptitudeTopicService(AptitudeTopicRepository aptitudeTopicRepository) {
        this.aptitudeTopicRepository = aptitudeTopicRepository;
    }

    /**
     * Retrieves all 32 aptitude topics from the database.
     * @return List of AptitudeTopic entities.
     */
    public List<AptitudeTopic> getAllTopics() {
        return aptitudeTopicRepository.findAll();
    }
}
