package com.remoteprep.service;

import com.remoteprep.dto.AptitudeQuestionResponse;
import com.remoteprep.dto.GenerateExamRequest;
import com.remoteprep.dto.GenerateExamResponse;
import com.remoteprep.entity.AptitudeQuestion;
import com.remoteprep.entity.AptitudeTopic;
import com.remoteprep.entity.Assessment;
import com.remoteprep.repository.AptitudeQuestionRepository;
import com.remoteprep.repository.AptitudeTopicRepository;
import com.remoteprep.repository.AssessmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service Layer for Aptitude Exam Question Retrieval and Randomized Generation.
 */
@Service
public class AptitudeQuestionService {

    private final AptitudeQuestionRepository aptitudeQuestionRepository;
    private final AptitudeTopicRepository aptitudeTopicRepository;
    private final AssessmentRepository assessmentRepository;

    public AptitudeQuestionService(AptitudeQuestionRepository aptitudeQuestionRepository,
                                  AptitudeTopicRepository aptitudeTopicRepository,
                                  AssessmentRepository assessmentRepository) {
        this.aptitudeQuestionRepository = aptitudeQuestionRepository;
        this.aptitudeTopicRepository = aptitudeTopicRepository;
        this.assessmentRepository = assessmentRepository;
    }

    /**
     * Generates a 20-question randomized aptitude exam for an active assessment attempt.
     */
    @Transactional(readOnly = true)
    public GenerateExamResponse generateExam(GenerateExamRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }

        Long assessmentId = request.getAssessmentId();
        if (assessmentId == null) {
            throw new IllegalArgumentException("Assessment ID must be provided");
        }

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with ID: " + assessmentId));

        if (!"IN_PROGRESS".equalsIgnoreCase(assessment.getStatus())) {
            throw new IllegalStateException("Assessment is not in IN_PROGRESS state (current status: " + assessment.getStatus() + ")");
        }

        if (assessment.getStudent() == null) {
            throw new IllegalStateException("Assessment is not associated with any student");
        }

        List<Long> topicIds = request.getTopicIds();
        if (topicIds == null || topicIds.isEmpty()) {
            throw new IllegalArgumentException("At least 1 aptitude topic must be selected");
        }

        if (topicIds.size() > 3) {
            throw new IllegalArgumentException("Maximum of 3 aptitude topics can be selected, but received " + topicIds.size());
        }

        Set<Long> uniqueTopics = new HashSet<>(topicIds);
        if (uniqueTopics.size() != topicIds.size()) {
            throw new IllegalArgumentException("Duplicate topic IDs are not allowed");
        }

        // Validate each topic ID exists in aptitude_topics
        for (Long topicId : topicIds) {
            if (topicId == null || !aptitudeTopicRepository.existsById(topicId)) {
                throw new IllegalArgumentException("Invalid topic ID: " + topicId);
            }
        }

        // Calculate distribution:
        // 1 topic -> 20
        // 2 topics -> 10, 10
        // 3 topics -> 7, 7, 6
        int[] distribution;
        if (topicIds.size() == 1) {
            distribution = new int[]{20};
        } else if (topicIds.size() == 2) {
            distribution = new int[]{10, 10};
        } else {
            distribution = new int[]{7, 7, 6};
        }

        // Verify question availability for each selected topic
        for (int i = 0; i < topicIds.size(); i++) {
            Long topicId = topicIds.get(i);
            int required = distribution[i];
            long available = aptitudeQuestionRepository.countByTopic_Id(topicId);

            if (available < required) {
                AptitudeTopic topic = aptitudeTopicRepository.findById(topicId).orElse(null);
                String topicName = topic != null ? topic.getTopicName() : String.valueOf(topicId);
                throw new IllegalStateException("Insufficient questions for topic '" + topicName +
                        "' (ID " + topicId + "). Required: " + required + ", Available: " + available);
            }
        }

        // Retrieve randomized questions per topic
        List<AptitudeQuestion> allSelectedQuestions = new ArrayList<>(20);
        Set<Long> uniqueQuestionIds = new HashSet<>(20);

        for (int i = 0; i < topicIds.size(); i++) {
            Long topicId = topicIds.get(i);
            int required = distribution[i];

            List<AptitudeQuestion> topicQuestions = aptitudeQuestionRepository.findRandomQuestionsByTopicId(topicId, required);

            for (AptitudeQuestion q : topicQuestions) {
                if (uniqueQuestionIds.add(q.getId())) {
                    allSelectedQuestions.add(q);
                }
            }
        }

        if (allSelectedQuestions.size() != 20) {
            throw new IllegalStateException("Expected exactly 20 questions, but selected " + allSelectedQuestions.size());
        }

        // Randomize overall question presentation order
        Collections.shuffle(allSelectedQuestions);

        // Map to client-safe DTOs (DO NOT expose correct answer or explanation)
        List<AptitudeQuestionResponse> questionDtos = new ArrayList<>(20);
        for (AptitudeQuestion q : allSelectedQuestions) {
            questionDtos.add(new AptitudeQuestionResponse(
                    q.getId(),
                    q.getTopicId(),
                    q.getQuestionText(),
                    q.getOptionA(),
                    q.getOptionB(),
                    q.getOptionC(),
                    q.getOptionD()
            ));
        }

        return new GenerateExamResponse(assessmentId, questionDtos);
    }
}
