package com.remoteprep.service;

import com.remoteprep.dto.SubmitDsaCodeRequest;
import com.remoteprep.dto.SubmitDsaCodeResponse;
import com.remoteprep.entity.Assessment;
import com.remoteprep.entity.DsaQuestion;
import com.remoteprep.entity.DsaSubmission;
import com.remoteprep.repository.AssessmentRepository;
import com.remoteprep.repository.DsaExamQuestionRepository;
import com.remoteprep.repository.DsaQuestionRepository;
import com.remoteprep.repository.DsaSubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Service Layer for candidate DSA code submissions.
 * Handles validation, assignment verification against 'dsa_exam_questions',
 * and submission persistence to 'dsa_submissions'.
 */
@Service
public class DsaSubmissionService {

    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("JAVA", "CPP", "C", "PYTHON");

    private final AssessmentRepository assessmentRepository;
    private final DsaExamQuestionRepository dsaExamQuestionRepository;
    private final DsaQuestionRepository dsaQuestionRepository;
    private final DsaSubmissionRepository dsaSubmissionRepository;

    public DsaSubmissionService(AssessmentRepository assessmentRepository,
                                DsaExamQuestionRepository dsaExamQuestionRepository,
                                DsaQuestionRepository dsaQuestionRepository,
                                DsaSubmissionRepository dsaSubmissionRepository) {
        this.assessmentRepository = assessmentRepository;
        this.dsaExamQuestionRepository = dsaExamQuestionRepository;
        this.dsaQuestionRepository = dsaQuestionRepository;
        this.dsaSubmissionRepository = dsaSubmissionRepository;
    }

    /**
     * Persists an actual candidate code submission for an assigned DSA question.
     * Starts with resultStatus='PENDING' awaiting Phase 10 execution.
     */
    @Transactional
    public SubmitDsaCodeResponse submitCode(SubmitDsaCodeRequest request) {
        // 1. Validate request body and required fields
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }

        Long assessmentId = request.getAssessmentId();
        if (assessmentId == null) {
            throw new IllegalArgumentException("assessmentId must be provided");
        }

        Long questionId = request.getQuestionId();
        if (questionId == null) {
            throw new IllegalArgumentException("questionId must be provided");
        }

        String rawLanguage = request.getLanguage();
        if (rawLanguage == null || rawLanguage.trim().isEmpty()) {
            throw new IllegalArgumentException("language must be provided");
        }

        String sourceCode = request.getSourceCode();
        if (sourceCode == null) {
            throw new IllegalArgumentException("sourceCode cannot be null");
        }

        if (sourceCode.trim().isEmpty()) {
            throw new IllegalArgumentException("sourceCode cannot be blank");
        }

        // 2. Normalize and validate programming language
        String normalizedLanguage = rawLanguage.trim().toUpperCase();
        if (!SUPPORTED_LANGUAGES.contains(normalizedLanguage)) {
            throw new IllegalArgumentException("Unsupported programming language: " + rawLanguage +
                    ". Supported languages are: JAVA, CPP, C, PYTHON");
        }

        // 3. Validate assessment exists, is IN_PROGRESS, and has a student
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with ID: " + assessmentId));

        if (!"IN_PROGRESS".equalsIgnoreCase(assessment.getStatus())) {
            throw new IllegalStateException("Assessment is not in IN_PROGRESS state (current status: " + assessment.getStatus() + ")");
        }

        if (assessment.getStudent() == null) {
            throw new IllegalStateException("Assessment is not associated with any student");
        }

        // 4. Validate question exists
        DsaQuestion question = dsaQuestionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("DSA Question not found with ID: " + questionId));

        // 5. Verify question was actually assigned to THIS assessment via dsa_exam_questions
        boolean isAssigned = dsaExamQuestionRepository.existsByAssessment_IdAndQuestion_Id(assessmentId, questionId);
        if (!isAssigned) {
            throw new IllegalArgumentException("Question ID " + questionId + " is not assigned to assessment ID " + assessmentId);
        }

        // 6. Persist new submission in dsa_submissions with PENDING status
        DsaSubmission submission = new DsaSubmission();
        submission.setAssessment(assessment);
        submission.setQuestion(question);
        submission.setLanguage(normalizedLanguage);
        submission.setSourceCode(sourceCode);
        submission.setResultStatus("PENDING");
        submission.setSubmittedAt(LocalDateTime.now());

        DsaSubmission saved = dsaSubmissionRepository.save(submission);

        // 7. Return safe client response DTO (no test_cases, no entity internals)
        return new SubmitDsaCodeResponse(
                saved.getId(),
                assessment.getId(),
                question.getId(),
                saved.getLanguage(),
                saved.getResultStatus(),
                saved.getSubmittedAt()
        );
    }
}
