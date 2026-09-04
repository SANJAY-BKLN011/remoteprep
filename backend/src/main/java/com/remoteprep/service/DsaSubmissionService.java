package com.remoteprep.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.remoteprep.dto.SubmitDsaCodeRequest;
import com.remoteprep.dto.SubmitDsaCodeResponse;
import com.remoteprep.entity.Assessment;
import com.remoteprep.entity.DsaQuestion;
import com.remoteprep.entity.DsaSubmission;
import com.remoteprep.execution.CodeExecutionService;
import com.remoteprep.execution.ExecutionRequest;
import com.remoteprep.execution.ExecutionResult;
import com.remoteprep.execution.ExecutionStatus;
import com.remoteprep.repository.AssessmentRepository;
import com.remoteprep.repository.DsaExamQuestionRepository;
import com.remoteprep.repository.DsaQuestionRepository;
import com.remoteprep.repository.DsaSubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Service Layer for candidate DSA code submissions (Phase 12).
 * Handles validation, assignment verification, creates PENDING submission,
 * executes candidate code across all test cases (visible and hidden) outside DB transactions,
 * determines the final judging verdict, and persists the result.
 */
@Service
public class DsaSubmissionService {

    private static final Logger log = LoggerFactory.getLogger(DsaSubmissionService.class);
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("JAVA", "CPP", "C", "PYTHON");

    private final AssessmentRepository assessmentRepository;
    private final DsaExamQuestionRepository dsaExamQuestionRepository;
    private final DsaQuestionRepository dsaQuestionRepository;
    private final DsaSubmissionRepository dsaSubmissionRepository;
    private final CodeExecutionService codeExecutionService;
    private final DsaOutputComparator outputComparator;
    private final ObjectMapper objectMapper;

    public DsaSubmissionService(AssessmentRepository assessmentRepository,
                                DsaExamQuestionRepository dsaExamQuestionRepository,
                                DsaQuestionRepository dsaQuestionRepository,
                                DsaSubmissionRepository dsaSubmissionRepository,
                                CodeExecutionService codeExecutionService,
                                DsaOutputComparator outputComparator,
                                ObjectMapper objectMapper) {
        this.assessmentRepository = assessmentRepository;
        this.dsaExamQuestionRepository = dsaExamQuestionRepository;
        this.dsaQuestionRepository = dsaQuestionRepository;
        this.dsaSubmissionRepository = dsaSubmissionRepository;
        this.codeExecutionService = codeExecutionService;
        this.outputComparator = outputComparator;
        this.objectMapper = objectMapper;
    }

    /**
     * Submits and judges candidate code for an assigned DSA question.
     * Execution happens outside of long-running database transactions.
     *
     * @param request the submission request containing assessmentId, questionId, language, and sourceCode
     * @return safe response summarizing judging outcome without revealing hidden test data
     */
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

        // 3. Validate assessment exists, is IN_PROGRESS, and has an associated student
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

        // 5. Verify question was assigned to THIS assessment via dsa_exam_questions
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

        submission = dsaSubmissionRepository.save(submission);

        // 7. Extract ALL final test cases (visible samples and hidden cases)
        List<JudgeTestCase> allTestCases = extractAllTestCases(question);
        int totalTestCases = allTestCases.size();
        int passedTestCases = 0;
        long totalExecutionTimeMs = 0L;
        String finalVerdict = "ACCEPTED";

        // 8. Execute candidate code independently against each test case (outside long DB transaction)
        try {
            for (JudgeTestCase tc : allTestCases) {
                ExecutionRequest execReq = new ExecutionRequest(sourceCode, normalizedLanguage, tc.getInput());
                ExecutionResult execRes = codeExecutionService.executeCode(execReq);
                totalExecutionTimeMs += execRes.getExecutionTimeMs();

                ExecutionStatus status = execRes.getStatus();
                if (status == ExecutionStatus.COMPILATION_ERROR) {
                    finalVerdict = "COMPILATION_ERROR";
                    break;
                } else if (status == ExecutionStatus.RUNTIME_ERROR) {
                    finalVerdict = "RUNTIME_ERROR";
                    break;
                } else if (status == ExecutionStatus.TIME_LIMIT_EXCEEDED) {
                    finalVerdict = "TIME_LIMIT_EXCEEDED";
                    break;
                } else if (status == ExecutionStatus.OUTPUT_LIMIT_EXCEEDED) {
                    finalVerdict = "OUTPUT_LIMIT_EXCEEDED";
                    break;
                } else if (status == ExecutionStatus.EXECUTION_ERROR) {
                    finalVerdict = "EXECUTION_ERROR";
                    break;
                } else if (status == ExecutionStatus.SUCCESS) {
                    boolean matches = outputComparator.matches(tc.getExpectedOutput(), execRes.getStdout());
                    if (matches) {
                        passedTestCases++;
                    } else {
                        finalVerdict = "WRONG_ANSWER";
                        break;
                    }
                } else {
                    finalVerdict = "EXECUTION_ERROR";
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Unexpected error judging submission ID {}: {}", submission.getId(), e.getMessage(), e);
            finalVerdict = "EXECUTION_ERROR";
        }

        int failedTestCases = "ACCEPTED".equals(finalVerdict) ? 0 : (totalTestCases - passedTestCases);

        // 9. Update the submission row with the final evaluation verdict
        submission.setResultStatus(finalVerdict);
        dsaSubmissionRepository.save(submission);

        // 10. Return safe response without exposing hidden test data
        return new SubmitDsaCodeResponse(
                submission.getId(),
                assessment.getId(),
                question.getId(),
                normalizedLanguage,
                finalVerdict,
                submission.getSubmittedAt(),
                totalTestCases,
                passedTestCases,
                failedTestCases,
                totalExecutionTimeMs
        );
    }

    private List<JudgeTestCase> extractAllTestCases(DsaQuestion question) {
        List<JudgeTestCase> cases = new ArrayList<>();

        // 1. Try parsing test_cases column (contains "sample" and "hidden")
        String testCasesJson = question.getTestCases();
        if (testCasesJson != null && !testCasesJson.isBlank()) {
            try {
                JsonNode root = objectMapper.readTree(testCasesJson);

                // Sample (visible) cases
                JsonNode sampleNode = root.isObject() ? root.get("sample") : (root.isArray() ? root : null);
                if (sampleNode != null && sampleNode.isArray()) {
                    for (JsonNode node : sampleNode) {
                        String input = node.has("input") ? node.get("input").asText() : "";
                        String expectedOutput = node.has("expectedOutput") ? node.get("expectedOutput").asText()
                                : (node.has("output") ? node.get("output").asText() : "");
                        if (!input.isBlank() || !expectedOutput.isBlank()) {
                            cases.add(new JudgeTestCase(input, expectedOutput, false));
                        }
                    }
                }

                // Hidden cases
                JsonNode hiddenNode = root.isObject() ? root.get("hidden") : null;
                if (hiddenNode != null && hiddenNode.isArray()) {
                    for (JsonNode node : hiddenNode) {
                        String input = node.has("input") ? node.get("input").asText() : "";
                        String expectedOutput = node.has("expectedOutput") ? node.get("expectedOutput").asText()
                                : (node.has("output") ? node.get("output").asText() : "");
                        if (!input.isBlank() || !expectedOutput.isBlank()) {
                            cases.add(new JudgeTestCase(input, expectedOutput, true));
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Failed to parse test_cases JSON for question ID {}: {}", question.getId(), e.getMessage());
            }
        }

        // 2. Fallback: Parse visible cases from examples column if no samples in test_cases
        boolean hasVisibleCases = cases.stream().anyMatch(c -> !c.isHidden());
        if (!hasVisibleCases && question.getExamples() != null && !question.getExamples().isBlank()) {
            try {
                JsonNode examplesNode = objectMapper.readTree(question.getExamples());
                if (examplesNode.isArray()) {
                    for (JsonNode node : examplesNode) {
                        String input = node.has("input") ? node.get("input").asText() : "";
                        String output = node.has("output") ? node.get("output").asText()
                                : (node.has("expectedOutput") ? node.get("expectedOutput").asText() : "");
                        if (!input.isBlank() || !output.isBlank()) {
                            cases.add(new JudgeTestCase(input, output, false));
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Failed to parse examples JSON for question ID {}: {}", question.getId(), e.getMessage());
            }
        }

        if (cases.isEmpty()) {
            throw new IllegalStateException("DSA Question ID " + question.getId() + " does not have any test cases configured");
        }

        return cases;
    }

    private static class JudgeTestCase {
        private final String input;
        private final String expectedOutput;
        private final boolean hidden;

        JudgeTestCase(String input, String expectedOutput, boolean hidden) {
            this.input = input;
            this.expectedOutput = expectedOutput;
            this.hidden = hidden;
        }

        public String getInput() {
            return input;
        }

        public String getExpectedOutput() {
            return expectedOutput;
        }

        public boolean isHidden() {
            return hidden;
        }
    }
}
