package com.remoteprep.service;

import com.remoteprep.dto.DsaQuestionResponse;
import com.remoteprep.dto.GenerateDsaExamRequest;
import com.remoteprep.dto.GenerateDsaExamResponse;
import com.remoteprep.dto.StarterCodeDto;
import com.remoteprep.entity.Assessment;
import com.remoteprep.entity.DsaExamQuestion;
import com.remoteprep.entity.DsaQuestion;
import com.remoteprep.repository.AssessmentRepository;
import com.remoteprep.repository.DsaExamQuestionRepository;
import com.remoteprep.repository.DsaQuestionRepository;
import com.remoteprep.repository.DsaTopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service Layer for DSA Question Retrieval and Randomized Exam Generation.
 * Persists assignments exclusively to 'dsa_exam_questions', preserving
 * 'dsa_submissions' solely for actual candidate source-code submissions.
 */
@Service
public class DsaQuestionService {

    private final DsaQuestionRepository dsaQuestionRepository;
    private final DsaTopicRepository dsaTopicRepository;
    private final AssessmentRepository assessmentRepository;
    private final DsaExamQuestionRepository dsaExamQuestionRepository;

    public DsaQuestionService(DsaQuestionRepository dsaQuestionRepository,
                              DsaTopicRepository dsaTopicRepository,
                              AssessmentRepository assessmentRepository,
                              DsaExamQuestionRepository dsaExamQuestionRepository) {
        this.dsaQuestionRepository = dsaQuestionRepository;
        this.dsaTopicRepository = dsaTopicRepository;
        this.assessmentRepository = assessmentRepository;
        this.dsaExamQuestionRepository = dsaExamQuestionRepository;
    }

    /**
     * Generates or retrieves the assigned DSA exam for an active assessment.
     * Guarantees exactly 1 EASY problem and 1 MEDIUM problem.
     * Persists assignment to 'dsa_exam_questions' to ensure idempotency so repeated
     * requests for the same assessment return the identical question pair.
     * Never creates fake rows in 'dsa_submissions'.
     */
    @Transactional
    public GenerateDsaExamResponse generateDsaExam(GenerateDsaExamRequest request) {
        // 1. Validate request body and assessmentId
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }

        Long assessmentId = request.getAssessmentId();
        if (assessmentId == null) {
            throw new IllegalArgumentException("assessmentId must be provided");
        }

        // 2. Validate assessment exists and is IN_PROGRESS
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with ID: " + assessmentId));

        if (!"IN_PROGRESS".equalsIgnoreCase(assessment.getStatus())) {
            throw new IllegalStateException("Assessment is not in IN_PROGRESS state (current status: " + assessment.getStatus() + ")");
        }

        if (assessment.getStudent() == null) {
            throw new IllegalStateException("Assessment is not associated with any student");
        }

        // 3. Idempotency check: If DSA questions have already been assigned, return the SAME two questions
        List<DsaExamQuestion> existingAssignments = dsaExamQuestionRepository.findByAssessment_IdOrderByQuestionOrderAsc(assessmentId);
        if (existingAssignments != null && existingAssignments.size() == 2) {
            List<DsaQuestionResponse> assignedQuestions = new ArrayList<>(2);
            for (DsaExamQuestion assignment : existingAssignments) {
                assignedQuestions.add(toDsaQuestionResponse(assignment.getQuestion()));
            }
            return new GenerateDsaExamResponse(assessmentId, assignedQuestions);
        }

        // 4. Validate topic IDs
        List<Long> topicIds = request.getTopicIds();
        if (topicIds == null) {
            throw new IllegalArgumentException("Topic list cannot be null");
        }

        if (topicIds.isEmpty()) {
            throw new IllegalArgumentException("At least one DSA topic must be selected");
        }

        Set<Long> uniqueTopics = new HashSet<>(topicIds);
        if (uniqueTopics.size() != topicIds.size()) {
            throw new IllegalArgumentException("Duplicate topic IDs are not permitted");
        }

        for (Long topicId : topicIds) {
            if (topicId == null || !dsaTopicRepository.existsById(topicId)) {
                throw new IllegalArgumentException("Invalid DSA topic ID: " + topicId);
            }
        }

        // 5. Check question availability for EASY and MEDIUM
        long easyCount = dsaQuestionRepository.countByTopic_IdInAndDifficulty(topicIds, "EASY");
        if (easyCount == 0) {
            throw new IllegalStateException("No EASY question available for the selected topic(s)");
        }

        long mediumCount = dsaQuestionRepository.countByTopic_IdInAndDifficulty(topicIds, "MEDIUM");
        if (mediumCount == 0) {
            throw new IllegalStateException("No MEDIUM question available for the selected topic(s)");
        }

        // 6. Randomly select 1 EASY question and 1 MEDIUM question
        DsaQuestion easyQuestion = dsaQuestionRepository.findRandomQuestionByTopicIdsAndDifficulty(topicIds, "EASY")
                .orElseThrow(() -> new IllegalStateException("Failed to retrieve EASY question"));

        DsaQuestion mediumQuestion = dsaQuestionRepository.findRandomQuestionByTopicIdsAndDifficulty(topicIds, "MEDIUM")
                .orElseThrow(() -> new IllegalStateException("Failed to retrieve MEDIUM question"));

        // 7. Persist assigned questions in dsa_exam_questions (order 1 for EASY, 2 for MEDIUM)
        DsaExamQuestion assignment1 = new DsaExamQuestion(assessment, easyQuestion, 1);
        DsaExamQuestion assignment2 = new DsaExamQuestion(assessment, mediumQuestion, 2);

        dsaExamQuestionRepository.save(assignment1);
        dsaExamQuestionRepository.save(assignment2);

        // 8. Map to client-safe DTOs (DO NOT expose test_cases)
        List<DsaQuestionResponse> questionDtos = List.of(
                toDsaQuestionResponse(easyQuestion),
                toDsaQuestionResponse(mediumQuestion)
        );

        return new GenerateDsaExamResponse(assessmentId, questionDtos);
    }

    private DsaQuestionResponse toDsaQuestionResponse(DsaQuestion q) {
        StarterCodeDto starter = new StarterCodeDto(
                q.getStarterJava(),
                q.getStarterCpp(),
                q.getStarterC(),
                q.getStarterPython()
        );

        return new DsaQuestionResponse(
                q.getId(),
                q.getTopicId(),
                q.getDifficulty(),
                q.getTitle(),
                q.getDescription(),
                q.getExamples(),
                q.getConstraints(),
                starter
        );
    }
}
