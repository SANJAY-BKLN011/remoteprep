package com.remoteprep;

import com.remoteprep.dto.DsaRunRequest;
import com.remoteprep.dto.DsaRunResponse;
import com.remoteprep.dto.DsaRunTestCaseResult;
import com.remoteprep.dto.GenerateDsaExamRequest;
import com.remoteprep.dto.StartAssessmentRequest;
import com.remoteprep.dto.StartAssessmentResponse;
import com.remoteprep.dto.SubmitDsaCodeRequest;
import com.remoteprep.dto.SubmitDsaCodeResponse;
import com.remoteprep.entity.Assessment;
import com.remoteprep.entity.DsaExamQuestion;
import com.remoteprep.execution.ExecutionProperties;
import com.remoteprep.execution.ExecutionStatus;
import com.remoteprep.execution.ToolAvailabilityChecker;
import com.remoteprep.repository.AssessmentRepository;
import com.remoteprep.repository.DsaExamQuestionRepository;
import com.remoteprep.repository.DsaQuestionRepository;
import com.remoteprep.repository.DsaSubmissionRepository;
import com.remoteprep.service.AssessmentService;
import com.remoteprep.service.DsaQuestionService;
import com.remoteprep.service.DsaRunService;
import com.remoteprep.service.DsaSubmissionService;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DsaRunTests {

    @Autowired
    private DsaRunService dsaRunService;

    @Autowired
    private DsaQuestionService dsaQuestionService;

    @Autowired
    private DsaSubmissionService dsaSubmissionService;

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private DsaExamQuestionRepository dsaExamQuestionRepository;

    @Autowired
    private DsaSubmissionRepository dsaSubmissionRepository;

    @Autowired
    private DsaQuestionRepository dsaQuestionRepository;

    @Autowired
    private ExecutionProperties executionProperties;

    private Long createAssessmentWithExam(String rollNumber, List<Long> topics) {
        StartAssessmentResponse startRes = assessmentService.startAssessment(
                new StartAssessmentRequest("Student " + rollNumber, rollNumber)
        );
        Long assessmentId = startRes.getAssessmentId();
        dsaQuestionService.generateDsaExam(new GenerateDsaExamRequest(assessmentId, topics));
        return assessmentId;
    }

    private Long getAssignedQuestionId(Long assessmentId, int index) {
        List<DsaExamQuestion> assigned = dsaExamQuestionRepository.findByAssessment_IdOrderByQuestionOrderAsc(assessmentId);
        return assigned.get(index).getQuestion().getId();
    }

    @Test
    @DisplayName("Tests 1, 5, 8, 9, 10: Valid Java Run request executes exactly TWO demo cases without DB persistence")
    void testValidJavaRunRequest() {
        Long assessmentId = createAssessmentWithExam("RUN_JAVA_01", List.of(1L));
        Long qId = getAssignedQuestionId(assessmentId, 0);

        long submissionsBefore = dsaSubmissionRepository.countByAssessment_Id(assessmentId);
        Assessment assessmentBefore = assessmentRepository.findById(assessmentId).orElseThrow();
        int scoreBefore = assessmentBefore.getDsaScore();

        String javaCode = "import java.util.Scanner;\n" +
                "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        Scanner sc = new Scanner(System.in);\n" +
                "        if (sc.hasNext()) {\n" +
                "            System.out.println(sc.nextLine());\n" +
                "        } else {\n" +
                "            System.out.println(\"Demo Output\");\n" +
                "        }\n" +
                "    }\n" +
                "}";

        DsaRunResponse response = dsaRunService.runCode(new DsaRunRequest(assessmentId, qId, "JAVA", javaCode));

        // Exactly 2 demo test cases returned
        assertNotNull(response);
        assertEquals(assessmentId, response.getAssessmentId());
        assertEquals(qId, response.getQuestionId());
        assertEquals("JAVA", response.getLanguage());
        assertNotNull(response.getTestCases());
        assertEquals(2, response.getTestCases().size(), "Run must execute and return exactly TWO demo test cases");

        for (int i = 0; i < 2; i++) {
            DsaRunTestCaseResult tc = response.getTestCases().get(i);
            assertEquals(i + 1, tc.getTestCaseNumber());
            assertNotNull(tc.getInput());
            assertNotNull(tc.getExpectedOutput());
            assertNotNull(tc.getActualOutput());
            assertEquals("SUCCESS", tc.getStatus());
            assertNotNull(tc.getExecutionTimeMs());
        }

        // Verify NO dsa_submissions rows created
        long submissionsAfter = dsaSubmissionRepository.countByAssessment_Id(assessmentId);
        assertEquals(submissionsBefore, submissionsAfter, "Run must NOT create any dsa_submissions rows!");

        // Verify NO assessment score or state modification
        Assessment assessmentAfter = assessmentRepository.findById(assessmentId).orElseThrow();
        assertEquals(scoreBefore, assessmentAfter.getDsaScore(), "Run must NOT modify assessment score!");
        assertEquals("IN_PROGRESS", assessmentAfter.getStatus());
    }

    @Test
    @DisplayName("Tests 2, 6, 7: Valid Python Run request captures stdin and stdout")
    void testValidPythonRunRequest() {
        Assumptions.assumeTrue(ToolAvailabilityChecker.isToolAvailable(executionProperties.getPythonRuntime()),
                "Python interpreter not available in PATH");

        Long assessmentId = createAssessmentWithExam("RUN_PYTHON_01", List.of(2L));
        Long qId = getAssignedQuestionId(assessmentId, 0);

        String pythonCode = "import sys\n" +
                "line = sys.stdin.read().strip()\n" +
                "print(f'ECHO: {line}')\n";

        DsaRunResponse response = dsaRunService.runCode(new DsaRunRequest(assessmentId, qId, "python", pythonCode));

        assertEquals("PYTHON", response.getLanguage(), "Language must be normalized to uppercase PYTHON");
        assertEquals(2, response.getTestCases().size());

        DsaRunTestCaseResult tc1 = response.getTestCases().get(0);
        assertEquals("SUCCESS", tc1.getStatus());
        assertTrue(tc1.getActualOutput().contains("ECHO:"), "Output must contain captured echo from stdin");
    }

    @Test
    @DisplayName("Test 3: Valid C Run request (conditional on gcc)")
    void testValidCRunRequest() {
        Assumptions.assumeTrue(ToolAvailabilityChecker.isToolAvailable(executionProperties.getCCompiler()),
                "gcc not available in PATH");

        Long assessmentId = createAssessmentWithExam("RUN_C_01", List.of(3L));
        Long qId = getAssignedQuestionId(assessmentId, 0);

        String cCode = "#include <stdio.h>\nint main() { printf(\"Hello C Demo\\n\"); return 0; }";
        DsaRunResponse response = dsaRunService.runCode(new DsaRunRequest(assessmentId, qId, "C", cCode));

        assertEquals(2, response.getTestCases().size());
        assertEquals("SUCCESS", response.getTestCases().get(0).getStatus());
    }

    @Test
    @DisplayName("Test 4: Valid C++ Run request (conditional on g++)")
    void testValidCppRunRequest() {
        Assumptions.assumeTrue(ToolAvailabilityChecker.isToolAvailable(executionProperties.getCppCompiler()),
                "g++ not available in PATH");

        Long assessmentId = createAssessmentWithExam("RUN_CPP_01", List.of(4L));
        Long qId = getAssignedQuestionId(assessmentId, 0);

        String cppCode = "#include <iostream>\nint main() { std::cout << \"Hello CPP Demo\" << std::endl; return 0; }";
        DsaRunResponse response = dsaRunService.runCode(new DsaRunRequest(assessmentId, qId, "CPP", cppCode));

        assertEquals(2, response.getTestCases().size());
        assertEquals("SUCCESS", response.getTestCases().get(0).getStatus());
    }

    @Test
    @DisplayName("Tests 11, 12, 13, 15, 16, 17: Request and resource validation errors")
    void testValidationRejections() {
        Long assessmentId = createAssessmentWithExam("RUN_VAL_01", List.of(1L));
        Long qId = getAssignedQuestionId(assessmentId, 0);

        // Null request
        assertThrows(IllegalArgumentException.class, () -> dsaRunService.runCode(null));

        // Missing assessmentId
        assertThrows(IllegalArgumentException.class, () ->
                dsaRunService.runCode(new DsaRunRequest(null, qId, "JAVA", "code")));

        // Missing questionId
        assertThrows(IllegalArgumentException.class, () ->
                dsaRunService.runCode(new DsaRunRequest(assessmentId, null, "JAVA", "code")));

        // Missing language (15)
        assertThrows(IllegalArgumentException.class, () ->
                dsaRunService.runCode(new DsaRunRequest(assessmentId, qId, null, "code")));
        assertThrows(IllegalArgumentException.class, () ->
                dsaRunService.runCode(new DsaRunRequest(assessmentId, qId, "   ", "code")));

        // Unsupported language (16)
        assertThrows(IllegalArgumentException.class, () ->
                dsaRunService.runCode(new DsaRunRequest(assessmentId, qId, "RUBY", "code")));
        assertThrows(IllegalArgumentException.class, () ->
                dsaRunService.runCode(new DsaRunRequest(assessmentId, qId, "JAVASCRIPT", "code")));

        // Blank source code (17)
        assertThrows(IllegalArgumentException.class, () ->
                dsaRunService.runCode(new DsaRunRequest(assessmentId, qId, "JAVA", null)));
        assertThrows(IllegalArgumentException.class, () ->
                dsaRunService.runCode(new DsaRunRequest(assessmentId, qId, "JAVA", "   ")));

        // Nonexistent assessment (11)
        assertThrows(IllegalArgumentException.class, () ->
                dsaRunService.runCode(new DsaRunRequest(9999999L, qId, "JAVA", "code")));

        // Nonexistent question (13)
        assertThrows(IllegalArgumentException.class, () ->
                dsaRunService.runCode(new DsaRunRequest(assessmentId, 9999999L, "JAVA", "code")));

        // Completed assessment rejected (12)
        Assessment completedAssessment = assessmentRepository.findById(assessmentId).orElseThrow();
        completedAssessment.setStatus("COMPLETED");
        assessmentRepository.save(completedAssessment);
        assertThrows(IllegalStateException.class, () ->
                dsaRunService.runCode(new DsaRunRequest(assessmentId, qId, "JAVA", "code")));
    }

    @Test
    @DisplayName("Test 14: Question from another assessment strictly rejected")
    void testCrossAssessmentQuestionRejected() {
        // Topic 1 for Assessment A
        Long assessmentA = createAssessmentWithExam("RUN_CROSS_A", List.of(1L));
        // Topic 9 for Assessment B (disjoint topics ensure no question overlap)
        Long assessmentB = createAssessmentWithExam("RUN_CROSS_B", List.of(9L));

        Long questionB = getAssignedQuestionId(assessmentB, 0);

        // Question B is definitely NOT assigned to Assessment A
        assertFalse(dsaExamQuestionRepository.existsByAssessment_IdAndQuestion_Id(assessmentA, questionB));

        // Attempting to run question B under assessment A must be rejected
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                dsaRunService.runCode(new DsaRunRequest(assessmentA, questionB, "JAVA", "public class Main {}")));

        assertTrue(ex.getMessage().contains("is not assigned to assessment"));
    }

    @Test
    @DisplayName("Test 18: Compilation error returned correctly")
    void testCompilationErrorHandling() {
        Long assessmentId = createAssessmentWithExam("RUN_COMP_ERR", List.of(1L));
        Long qId = getAssignedQuestionId(assessmentId, 0);

        String invalidCode = "public class Main { public static void main(String[] args) { syntax_error; } }";
        DsaRunResponse response = dsaRunService.runCode(new DsaRunRequest(assessmentId, qId, "JAVA", invalidCode));

        assertEquals(2, response.getTestCases().size());
        assertEquals("COMPILATION_ERROR", response.getTestCases().get(0).getStatus());
        assertNotNull(response.getTestCases().get(0).getError());
        assertFalse(response.getTestCases().get(0).getError().isBlank());
    }

    @Test
    @DisplayName("Test 19: Runtime error returned correctly")
    void testRuntimeErrorHandling() {
        Long assessmentId = createAssessmentWithExam("RUN_RTE_01", List.of(1L));
        Long qId = getAssignedQuestionId(assessmentId, 0);

        String runtimeErrorCode = "public class Main { public static void main(String[] args) { int x = 1 / 0; } }";
        DsaRunResponse response = dsaRunService.runCode(new DsaRunRequest(assessmentId, qId, "JAVA", runtimeErrorCode));

        assertEquals(2, response.getTestCases().size());
        assertEquals("RUNTIME_ERROR", response.getTestCases().get(0).getStatus());
        assertTrue(response.getTestCases().get(0).getError().contains("ArithmeticException") ||
                response.getTestCases().get(0).getError().contains("/ by zero"));
    }

    @Test
    @DisplayName("Test 20: Infinite loop returns TIME_LIMIT_EXCEEDED")
    void testInfiniteLoopHandling() {
        Long assessmentId = createAssessmentWithExam("RUN_TLE_01", List.of(1L));
        Long qId = getAssignedQuestionId(assessmentId, 0);

        String loopCode = "public class Main { public static void main(String[] args) { while(true) {} } }";
        DsaRunResponse response = dsaRunService.runCode(new DsaRunRequest(assessmentId, qId, "JAVA", loopCode));

        assertEquals(2, response.getTestCases().size());
        assertEquals(ExecutionStatus.TIME_LIMIT_EXCEEDED.name(), response.getTestCases().get(0).getStatus());
    }

    @Test
    @DisplayName("Test 21: Excessive output returns OUTPUT_LIMIT_EXCEEDED")
    void testExcessiveOutputHandling() {
        Long assessmentId = createAssessmentWithExam("RUN_OLE_01", List.of(1L));
        Long qId = getAssignedQuestionId(assessmentId, 0);

        String floodCode = "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        for (int i = 0; i < 500000; i++) {\n" +
                "            System.out.println(\"Excessive output flood stream...\");\n" +
                "        }\n" +
                "    }\n" +
                "}";

        DsaRunResponse response = dsaRunService.runCode(new DsaRunRequest(assessmentId, qId, "JAVA", floodCode));

        assertTrue(response.getTestCases().get(0).getStatus().equals("OUTPUT_LIMIT_EXCEEDED") ||
                response.getTestCases().get(0).getActualOutput().length() <= executionProperties.getMaxOutputBytes() + 2048);
    }

    @Test
    @DisplayName("Test 22 & 23: Lowercase language normalized and source code preserved exactly")
    void testLanguageNormalizationAndCodePreservation() {
        Long assessmentId = createAssessmentWithExam("RUN_NORM_01", List.of(1L));
        Long qId = getAssignedQuestionId(assessmentId, 0);

        String code = "public class Main { public static void main(String[] args) { System.out.println(\"Exact\"); } }";
        DsaRunResponse response = dsaRunService.runCode(new DsaRunRequest(assessmentId, qId, "java", code));

        assertEquals("JAVA", response.getLanguage());
        assertEquals("Exact\n", response.getTestCases().get(0).getActualOutput().replace("\r\n", "\n"));
    }

    @Test
    @DisplayName("Test 24: No hidden test cases are exposed")
    void testNoHiddenTestCasesExposed() {
        Long assessmentId = createAssessmentWithExam("RUN_SEC_01", List.of(1L));
        Long qId = getAssignedQuestionId(assessmentId, 0);

        String code = "public class Main { public static void main(String[] args) {} }";
        DsaRunResponse response = dsaRunService.runCode(new DsaRunRequest(assessmentId, qId, "JAVA", code));

        assertEquals(2, response.getTestCases().size());
        for (DsaRunTestCaseResult tc : response.getTestCases()) {
            assertNotNull(tc.getInput());
            assertNotNull(tc.getExpectedOutput());
            assertFalse(tc.getInput().contains("hidden"), "Demo test cases must not expose hidden test cases");
        }
    }

    @Test
    @DisplayName("Test 25: Phase 9 POST /api/dsa/submit remains completely functional and unaffected")
    void testPhase9SubmitRemainsUnaffected() {
        Long assessmentId = createAssessmentWithExam("RUN_P9_COMPAT", List.of(1L));
        Long qId = getAssignedQuestionId(assessmentId, 0);

        // Phase 9 submit continues to persist code with PENDING status
        SubmitDsaCodeResponse subRes = dsaSubmissionService.submitCode(
                new SubmitDsaCodeRequest(assessmentId, qId, "JAVA", "public class Main {}")
        );

        assertNotNull(subRes.getSubmissionId());
        assertEquals("PENDING", subRes.getResultStatus());
        assertEquals(1, dsaSubmissionRepository.countByAssessment_Id(assessmentId));
    }
}
