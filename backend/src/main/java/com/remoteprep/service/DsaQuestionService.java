package com.remoteprep.service;

import com.remoteprep.dto.DsaQuestionResponse;
import com.remoteprep.dto.GenerateDsaExamRequest;
import com.remoteprep.dto.GenerateDsaExamResponse;
import com.remoteprep.dto.StarterCodeDto;
import com.remoteprep.entity.Assessment;
import com.remoteprep.entity.DsaQuestion;
import com.remoteprep.entity.DsaSubmission;
import com.remoteprep.repository.AssessmentRepository;
import com.remoteprep.repository.DsaQuestionRepository;
import com.remoteprep.repository.DsaSubmissionRepository;
import com.remoteprep.repository.DsaTopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service Layer for DSA Question Retrieval and Randomized Exam Generation.
 */
@Service
public class DsaQuestionService {

    private final DsaQuestionRepository dsaQuestionRepository;
    private final DsaTopicRepository dsaTopicRepository;
    private final AssessmentRepository assessmentRepository;
    private final DsaSubmissionRepository dsaSubmissionRepository;

    public DsaQuestionService(DsaQuestionRepository dsaQuestionRepository,
                              DsaTopicRepository dsaTopicRepository,
                              AssessmentRepository assessmentRepository,
                              DsaSubmissionRepository dsaSubmissionRepository) {
        this.dsaQuestionRepository = dsaQuestionRepository;
        this.dsaTopicRepository = dsaTopicRepository;
        this.assessmentRepository = assessmentRepository;
        this.dsaSubmissionRepository = dsaSubmissionRepository;
    }

    /**
     * Generates or retrieves the assigned DSA exam for an active assessment.
     * Guarantees exactly 1 EASY problem and 1 MEDIUM problem.
     * Persists assignment to 'dsa_submissions' (with result_status='UNATTEMPTED') to ensure
     * idempotency so repeated requests for the same assessment return the identical question pair.
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
        List<DsaSubmission> existingSubmissions = dsaSubmissionRepository.findByAssessment_Id(assessmentId);
        if (existingSubmissions != null && existingSubmissions.size() == 2) {
            List<DsaQuestionResponse> assignedQuestions = new ArrayList<>(2);
            for (DsaSubmission sub : existingSubmissions) {
                assignedQuestions.add(toDsaQuestionResponse(sub.getQuestion()));
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

        // 7. Persist assigned questions in dsa_submissions table as UNATTEMPTED
        LocalDateTime now = LocalDateTime.now();
        DsaSubmission easySubmission = new DsaSubmission(assessment, easyQuestion, "UNSELECTED", "", "UNATTEMPTED", now);
        DsaSubmission mediumSubmission = new DsaSubmission(assessment, mediumQuestion, "UNSELECTED", "", "UNATTEMPTED", now);

        dsaSubmissionRepository.save(easySubmission);
        dsaSubmissionRepository.save(mediumSubmission);

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
