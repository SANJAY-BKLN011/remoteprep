package com.remoteprep.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.remoteprep.dto.DsaRunRequest;
import com.remoteprep.dto.DsaRunResponse;
import com.remoteprep.dto.DsaRunTestCaseResult;
import com.remoteprep.entity.Assessment;
import com.remoteprep.entity.DsaQuestion;
import com.remoteprep.execution.CodeExecutionService;
import com.remoteprep.execution.ExecutionRequest;
import com.remoteprep.execution.ExecutionResult;
import com.remoteprep.execution.ExecutionStatus;
import com.remoteprep.repository.AssessmentRepository;
import com.remoteprep.repository.DsaExamQuestionRepository;
import com.remoteprep.repository.DsaQuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Service handling DSA "Run" requests.
 * Retrieves exactly two demo test cases, executes candidate code independently
 * against each using the Phase 10 execution engine, and returns execution metrics.
 * Strictly performs no database mutations (no submissions created, no scores updated).
 */
@Service
public class DsaRunService {

    private static final Logger log = LoggerFactory.getLogger(DsaRunService.class);
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("JAVA", "CPP", "C", "PYTHON");

    private final AssessmentRepository assessmentRepository;
    private final DsaExamQuestionRepository dsaExamQuestionRepository;
    private final DsaQuestionRepository dsaQuestionRepository;
    private final CodeExecutionService codeExecutionService;
    private final ObjectMapper objectMapper;

    public DsaRunService(AssessmentRepository assessmentRepository,
                         DsaExamQuestionRepository dsaExamQuestionRepository,
                         DsaQuestionRepository dsaQuestionRepository,
                         CodeExecutionService codeExecutionService,
                         ObjectMapper objectMapper) {
        this.assessmentRepository = assessmentRepository;
        this.dsaExamQuestionRepository = dsaExamQuestionRepository;
        this.dsaQuestionRepository = dsaQuestionRepository;
        this.codeExecutionService = codeExecutionService;
        this.objectMapper = objectMapper;
    }

    public DsaRunResponse runCode(DsaRunRequest request) {
        // 1. Validate request and inputs
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

        // 2. Normalize and validate language
        String normalizedLanguage = rawLanguage.trim().toUpperCase();
        if (!SUPPORTED_LANGUAGES.contains(normalizedLanguage)) {
            throw new IllegalArgumentException("Unsupported programming language: " + rawLanguage +
                    ". Supported languages are: JAVA, CPP, C, PYTHON");
        }

        // 3. Verify Assessment existence, IN_PROGRESS state, and student association
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with ID: " + assessmentId));

        if (!"IN_PROGRESS".equalsIgnoreCase(assessment.getStatus())) {
            throw new IllegalStateException("Assessment is not in IN_PROGRESS state (current status: " + assessment.getStatus() + ")");
        }

        if (assessment.getStudent() == null) {
            throw new IllegalStateException("Assessment is not associated with any student");
        }

        // 4. Verify Question existence
        DsaQuestion question = dsaQuestionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("DSA Question not found with ID: " + questionId));

        // 5. Verify question is assigned to THIS assessment via dsa_exam_questions
        boolean isAssigned = dsaExamQuestionRepository.existsByAssessment_IdAndQuestion_Id(assessmentId, questionId);
        if (!isAssigned) {
            throw new IllegalArgumentException("Question ID " + questionId + " is not assigned to assessment ID " + assessmentId);
        }

        // 6. Retrieve exactly TWO demo test cases from question data
        List<DemoTestCase> demoCases = extractDemoTestCases(question);

        // 7. Execute candidate code independently against each demo test case
        List<DsaRunTestCaseResult> results = new ArrayList<>();

        // Test Case 1 Execution
        DemoTestCase case1 = demoCases.get(0);
        ExecutionResult execResult1 = codeExecutionService.executeCode(
                new ExecutionRequest(sourceCode, normalizedLanguage, case1.input)
        );

        results.add(mapToTestCaseResult(1, case1, execResult1));

        // Optimization: If compilation failed, do not execute Test Case 2
        if (execResult1.getStatus() == ExecutionStatus.COMPILATION_ERROR) {
            DemoTestCase case2 = demoCases.get(1);
            results.add(new DsaRunTestCaseResult(
                    2,
                    case2.input,
                    case2.expectedOutput,
                    "",
                    ExecutionStatus.COMPILATION_ERROR.name(),
                    "Compilation failed",
                    0L
            ));
        } else {
            // Test Case 2 Execution
            DemoTestCase case2 = demoCases.get(1);
            ExecutionResult execResult2 = codeExecutionService.executeCode(
                    new ExecutionRequest(sourceCode, normalizedLanguage, case2.input)
            );
            results.add(mapToTestCaseResult(2, case2, execResult2));
        }

        return new DsaRunResponse(assessmentId, questionId, normalizedLanguage, results);
    }

    private DsaRunTestCaseResult mapToTestCaseResult(int testCaseNumber, DemoTestCase testCase, ExecutionResult execResult) {
        String errorMsg = (execResult.getStderr() != null && !execResult.getStderr().isBlank())
                ? execResult.getStderr().trim()
                : null;

        return new DsaRunTestCaseResult(
                testCaseNumber,
                testCase.input,
                testCase.expectedOutput,
                execResult.getStdout(),
                execResult.getStatus().name(),
                errorMsg,
                execResult.getExecutionTimeMs()
        );
    }

    private List<DemoTestCase> extractDemoTestCases(DsaQuestion question) {
        List<DemoTestCase> cases = new ArrayList<>();

        // 1. Try parsing sample test cases from test_cases column
        String testCasesJson = question.getTestCases();
        if (testCasesJson != null && !testCasesJson.isBlank()) {
            try {
                JsonNode root = objectMapper.readTree(testCasesJson);
                JsonNode sampleNode = root.get("sample");
                if (sampleNode != null && sampleNode.isArray()) {
                    for (JsonNode node : sampleNode) {
                        String input = node.has("input") ? node.get("input").asText() : "";
                        String expectedOutput = node.has("expectedOutput") ? node.get("expectedOutput").asText() : "";
                        cases.add(new DemoTestCase(input, expectedOutput));
                        if (cases.size() == 2) break;
                    }
                }
            } catch (Exception e) {
                log.debug("Failed to parse test_cases JSON for question ID {}: {}", question.getId(), e.getMessage());
            }
        }

        // 2. Fallback: Parse from examples column
        if (cases.size() < 2 && question.getExamples() != null && !question.getExamples().isBlank()) {
            try {
                JsonNode examplesNode = objectMapper.readTree(question.getExamples());
                if (examplesNode.isArray()) {
                    for (JsonNode node : examplesNode) {
                        if (cases.size() >= 2) break;
                        String input = node.has("input") ? node.get("input").asText() : "";
                        String output = node.has("output") ? node.get("output").asText()
                                : (node.has("expectedOutput") ? node.get("expectedOutput").asText() : "");
                        cases.add(new DemoTestCase(input, output));
                    }
                }
            } catch (Exception e) {
                log.debug("Failed to parse examples JSON for question ID {}: {}", question.getId(), e.getMessage());
            }
        }

        // 3. Guarantee exactly two demo test cases
        while (cases.size() < 2) {
            int caseNum = cases.size() + 1;
            cases.add(new DemoTestCase("Sample Input " + caseNum, "Sample Output " + caseNum));
        }

        return cases;
    }

    private static class DemoTestCase {
        final String input;
        final String expectedOutput;

        DemoTestCase(String input, String expectedOutput) {
            this.input = input;
            this.expectedOutput = expectedOutput;
        }
    }
}
