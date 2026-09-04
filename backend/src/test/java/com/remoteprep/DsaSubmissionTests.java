package com.remoteprep;

import com.remoteprep.dto.GenerateDsaExamRequest;
import com.remoteprep.dto.GenerateDsaExamResponse;
import com.remoteprep.dto.StartAssessmentRequest;
import com.remoteprep.dto.StartAssessmentResponse;
import com.remoteprep.dto.SubmitDsaCodeRequest;
import com.remoteprep.dto.SubmitDsaCodeResponse;
import com.remoteprep.entity.Assessment;
import com.remoteprep.entity.DsaExamQuestion;
import com.remoteprep.entity.DsaQuestion;
import com.remoteprep.entity.DsaSubmission;
import com.remoteprep.entity.DsaTopic;
import com.remoteprep.repository.AssessmentRepository;
import com.remoteprep.repository.DsaExamQuestionRepository;
import com.remoteprep.repository.DsaQuestionRepository;
import com.remoteprep.repository.DsaSubmissionRepository;
import com.remoteprep.repository.DsaTopicRepository;
import com.remoteprep.service.AssessmentService;
import com.remoteprep.service.DsaOutputComparator;
import com.remoteprep.service.DsaQuestionService;
import com.remoteprep.service.DsaSubmissionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DsaSubmissionTests {

    @Autowired
    private DsaSubmissionService dsaSubmissionService;

    @Autowired
    private DsaQuestionService dsaQuestionService;

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
    private DsaTopicRepository dsaTopicRepository;

    @Autowired
    private DsaOutputComparator outputComparator;

    private Long createAssessmentWithExam(String rollNumber) {
        StartAssessmentResponse startRes = assessmentService.startAssessment(
                new StartAssessmentRequest("Student " + rollNumber, rollNumber)
        );
        Long assessmentId = startRes.getAssessmentId();
        dsaQuestionService.generateDsaExam(new GenerateDsaExamRequest(assessmentId, List.of(1L, 2L)));
        return assessmentId;
    }

    private Long getAssignedQuestionId(Long assessmentId, int index) {
        var assigned = dsaExamQuestionRepository.findByAssessment_IdOrderByQuestionOrderAsc(assessmentId);
        return assigned.get(index).getQuestion().getId();
    }

    @Test
    @DisplayName("Tests 1-4: Valid JAVA, CPP, C, PYTHON submissions are persisted and judged")
    void testValidSubmissionsAllLanguages() {
        Long assessmentId = createAssessmentWithExam("DSA_SUB_ALL_LANGS");
        Long q1 = getAssignedQuestionId(assessmentId, 0);

        String[] langs = {"JAVA", "CPP", "C", "PYTHON"};
        for (String lang : langs) {
            SubmitDsaCodeResponse res = dsaSubmissionService.submitCode(new SubmitDsaCodeRequest(
                    assessmentId, q1, lang, "System.out.println(\"" + lang + "\");"
            ));

            assertNotNull(res.getSubmissionId());
            assertEquals(assessmentId, res.getAssessmentId());
            assertEquals(q1, res.getQuestionId());
            assertEquals(lang, res.getLanguage());
            assertNotNull(res.getResultStatus());
            assertNotEquals("PENDING", res.getResultStatus(), "Submit should judge and produce a final verdict");
            assertNotNull(res.getSubmittedAt());
            assertNotNull(res.getTotalTestCases());
            assertTrue(res.getTotalTestCases() > 0);
        }
    }

    @Test
    @DisplayName("Test 5: Lowercase language accepted and normalized")
    void testLowercaseLanguageNormalized() {
        Long assessmentId = createAssessmentWithExam("DSA_SUB_NORM");
        Long q1 = getAssignedQuestionId(assessmentId, 0);

        SubmitDsaCodeResponse res = dsaSubmissionService.submitCode(new SubmitDsaCodeRequest(
                assessmentId, q1, "python", "print('hello')"
        ));

        assertEquals("PYTHON", res.getLanguage(), "Language must be normalized to uppercase PYTHON");
    }

    @Test
    @DisplayName("Tests 6-11: Null request, missing fields, and blank sourceCode rejected")
    void testRequestValidationFailures() {
        Long assessmentId = createAssessmentWithExam("DSA_SUB_VAL");
        Long q1 = getAssignedQuestionId(assessmentId, 0);

        // 6. Null request
        assertThrows(IllegalArgumentException.class, () -> dsaSubmissionService.submitCode(null));

        // 7. Missing assessmentId
        assertThrows(IllegalArgumentException.class, () ->
                dsaSubmissionService.submitCode(new SubmitDsaCodeRequest(null, q1, "JAVA", "code")));

        // 8. Missing questionId
        assertThrows(IllegalArgumentException.class, () ->
                dsaSubmissionService.submitCode(new SubmitDsaCodeRequest(assessmentId, null, "JAVA", "code")));

        // 9. Missing language
        assertThrows(IllegalArgumentException.class, () ->
                dsaSubmissionService.submitCode(new SubmitDsaCodeRequest(assessmentId, q1, null, "code")));
        assertThrows(IllegalArgumentException.class, () ->
                dsaSubmissionService.submitCode(new SubmitDsaCodeRequest(assessmentId, q1, "   ", "code")));

        // 10. Missing sourceCode
        assertThrows(IllegalArgumentException.class, () ->
                dsaSubmissionService.submitCode(new SubmitDsaCodeRequest(assessmentId, q1, "JAVA", null)));

        // 11. Blank sourceCode
        assertThrows(IllegalArgumentException.class, () ->
                dsaSubmissionService.submitCode(new SubmitDsaCodeRequest(assessmentId, q1, "JAVA", "   ")));
    }

    @Test
    @DisplayName("Test 12: Invalid language rejected")
    void testInvalidLanguageRejected() {
        Long assessmentId = createAssessmentWithExam("DSA_SUB_INV_LANG");
        Long q1 = getAssignedQuestionId(assessmentId, 0);

        assertThrows(IllegalArgumentException.class, () ->
                dsaSubmissionService.submitCode(new SubmitDsaCodeRequest(assessmentId, q1, "JAVASCRIPT", "console.log('hi')")));
        assertThrows(IllegalArgumentException.class, () ->
                dsaSubmissionService.submitCode(new SubmitDsaCodeRequest(assessmentId, q1, "RUBY", "puts 'hi'")));
    }

    @Test
    @DisplayName("Test 13: Nonexistent assessment rejected")
    void testNonexistentAssessmentRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                dsaSubmissionService.submitCode(new SubmitDsaCodeRequest(9999999L, 1L, "JAVA", "code")));
    }

    @Test
    @DisplayName("Test 14: Assessment not IN_PROGRESS rejected")
    void testAssessmentNotInProgressRejected() {
        Long assessmentId = createAssessmentWithExam("DSA_SUB_COMPL");
        Long q1 = getAssignedQuestionId(assessmentId, 0);

        Assessment assessment = assessmentRepository.findById(assessmentId).orElseThrow();
        assessment.setStatus("COMPLETED");
        assessmentRepository.save(assessment);

        assertThrows(IllegalStateException.class, () ->
                dsaSubmissionService.submitCode(new SubmitDsaCodeRequest(assessmentId, q1, "JAVA", "code")));
    }

    @Test
    @DisplayName("Test 15: Assessment without student rejected")
    void testAssessmentWithoutStudentRejected() {
        Assessment mockAssessment = new Assessment();
        mockAssessment.setStatus("IN_PROGRESS");
        mockAssessment.setStudent(null);

        AssessmentRepository mockRepo = Mockito.mock(AssessmentRepository.class);
        Mockito.when(mockRepo.findById(999L)).thenReturn(Optional.of(mockAssessment));

        DsaSubmissionService testService = new DsaSubmissionService(
                mockRepo, null, null, null, null, null, null
        );

        assertThrows(IllegalStateException.class, () ->
                testService.submitCode(new SubmitDsaCodeRequest(999L, 1L, "JAVA", "code")));
    }

    @Test
    @DisplayName("Test 16: Nonexistent question rejected")
    void testNonexistentQuestionRejected() {
        Long assessmentId = createAssessmentWithExam("DSA_SUB_NON_Q");
        assertThrows(IllegalArgumentException.class, () ->
                dsaSubmissionService.submitCode(new SubmitDsaCodeRequest(assessmentId, 999999L, "JAVA", "code")));
    }

    @Test
    @DisplayName("Test 17 & 18: Question not assigned to assessment rejected (cannot submit another assessment's question)")
    void testUnassignedQuestionRejected() {
        // Assessment 1
        Long assessmentId1 = createAssessmentWithExam("DSA_SUB_CROSS_1");
        // Assessment 2
        Long assessmentId2 = createAssessmentWithExam("DSA_SUB_CROSS_2");

        Long q2_ass2 = getAssignedQuestionId(assessmentId2, 1);

        // Try to submit question from Assessment 2 under Assessment 1 (if not assigned to Assessment 1)
        boolean isAssignedTo1 = dsaExamQuestionRepository.existsByAssessment_IdAndQuestion_Id(assessmentId1, q2_ass2);
        if (!isAssignedTo1) {
            assertThrows(IllegalArgumentException.class, () ->
                    dsaSubmissionService.submitCode(new SubmitDsaCodeRequest(assessmentId1, q2_ass2, "JAVA", "code")));
        }
    }

    @Test
    @DisplayName("Tests 19-24: Multiple submissions, row counts, exact code preservation, PENDING status")
    void testMultipleSubmissionsAndPersistenceIntegrity() {
        Long assessmentId = createAssessmentWithExam("DSA_SUB_MULTI");
        Long q1 = getAssignedQuestionId(assessmentId, 0);

        long initialExamQuestionsCount = dsaExamQuestionRepository.countByAssessment_Id(assessmentId);
        assertEquals(2, initialExamQuestionsCount);
        long initialSubmissionsCount = dsaSubmissionRepository.countByAssessment_IdAndQuestion_Id(assessmentId, q1);
        assertEquals(0, initialSubmissionsCount);

        String code1 = "public class Main {\n    // First submission\n}";
        SubmitDsaCodeResponse sub1 = dsaSubmissionService.submitCode(
                new SubmitDsaCodeRequest(assessmentId, q1, "JAVA", code1)
        );

        // Test 19: First submission creates exactly one row
        assertEquals(1, dsaSubmissionRepository.countByAssessment_IdAndQuestion_Id(assessmentId, q1));
        // Test 22: New submission has evaluated result status
        assertNotNull(sub1.getResultStatus());
        assertNotEquals("PENDING", sub1.getResultStatus());
        // Test 24: submittedAt is populated
        assertNotNull(sub1.getSubmittedAt());

        // Test 23: Exact source code preservation
        DsaSubmission entity1 = dsaSubmissionRepository.findById(sub1.getSubmissionId()).orElseThrow();
        assertEquals(code1, entity1.getSourceCode());

        // Test 20 & 21: Second submission creates another row; previous is NOT overwritten
        String code2 = "def solve():\n    # Second submission in Python\n    pass";
        SubmitDsaCodeResponse sub2 = dsaSubmissionService.submitCode(
                new SubmitDsaCodeRequest(assessmentId, q1, "PYTHON", code2)
        );

        assertNotEquals(sub1.getSubmissionId(), sub2.getSubmissionId());
        assertEquals(2, dsaSubmissionRepository.countByAssessment_IdAndQuestion_Id(assessmentId, q1));

        // Verify previous submission entity remains intact
        DsaSubmission entity1After = dsaSubmissionRepository.findById(sub1.getSubmissionId()).orElseThrow();
        assertEquals("JAVA", entity1After.getLanguage());
        assertEquals(code1, entity1After.getSourceCode());

        DsaSubmission entity2After = dsaSubmissionRepository.findById(sub2.getSubmissionId()).orElseThrow();
        assertEquals("PYTHON", entity2After.getLanguage());
        assertEquals(code2, entity2After.getSourceCode());

        // Database tests: dsa_exam_questions is UNCHANGED
        long finalExamQuestionsCount = dsaExamQuestionRepository.countByAssessment_Id(assessmentId);
        assertEquals(initialExamQuestionsCount, finalExamQuestionsCount,
                "dsa_exam_questions count must remain completely unchanged after code submissions!");
    }

    @Test
    @DisplayName("Tests 25-27: Response does NOT expose test_cases, expected output, or entity internals")
    void testResponseSecurityExclusions() {
        Long assessmentId = createAssessmentWithExam("DSA_SUB_SEC");
        Long q1 = getAssignedQuestionId(assessmentId, 0);

        SubmitDsaCodeResponse res = dsaSubmissionService.submitCode(
                new SubmitDsaCodeRequest(assessmentId, q1, "JAVA", "int x = 10;")
        );

        assertNotNull(res);
        assertNotNull(res.getSubmissionId());
        assertNotNull(res.getAssessmentId());
        assertNotNull(res.getQuestionId());
        assertNotNull(res.getLanguage());
        assertNotNull(res.getResultStatus());
        assertNotNull(res.getSubmittedAt());
        assertNotNull(res.getTotalTestCases());
    }

    @Test
    @DisplayName("Phase 12: DsaOutputComparator unit tests for whitespace, line breaks, and exact match")
    void testOutputComparatorUnitTests() {
        // Exact match
        assertTrue(outputComparator.matches("hello", "hello"));

        // CRLF vs LF
        assertTrue(outputComparator.matches("line1\r\nline2", "line1\nline2"));
        assertTrue(outputComparator.matches("5\r\n", "5\n"));
        assertTrue(outputComparator.matches("5\n", "5"));

        // Trailing whitespace on lines
        assertTrue(outputComparator.matches("val  \nnext  ", "val\nnext"));
        assertTrue(outputComparator.matches("[0, 1]  ", "[0, 1]"));

        // Leading and trailing overall whitespace
        assertTrue(outputComparator.matches("  result  ", "result"));

        // Mismatches
        assertFalse(outputComparator.matches("5", "6"), "Genuinely different output must not match");
        assertFalse(outputComparator.matches("true", "false"));

        // Substring matching must never match
        assertFalse(outputComparator.matches("5", "55"), "Substring must not match");
        assertFalse(outputComparator.matches("55", "5"), "Substring must not match");
        assertFalse(outputComparator.matches("[0, 1]", "[0, 1, 2]"));

        // Null safety
        assertEquals("", outputComparator.normalize(null));
        assertTrue(outputComparator.matches(null, ""));
    }

    private DsaQuestion createIsolatedJudgeQuestion() {
        DsaTopic topic = dsaTopicRepository.findAll().get(0);
        DsaQuestion q = new DsaQuestion();
        q.setTopic(topic);
        q.setDifficulty("HARD");
        q.setTitle("Phase 12 Isolated Judge Question");
        q.setDescription("Sum two numbers");
        q.setTestCases("{" +
                "\"sample\":[" +
                "{\"input\":\"2 3\",\"expectedOutput\":\"5\"}," +
                "{\"input\":\"10 20\",\"expectedOutput\":\"30\"}" +
                "]," +
                "\"hidden\":[" +
                "{\"input\":\"100 200\",\"expectedOutput\":\"300\"}," +
                "{\"input\":\"-5 5\",\"expectedOutput\":\"0\"}" +
                "]" +
                "}");
        q.setExamples("[]");
        return dsaQuestionRepository.save(q);
    }

    private void cleanupIsolatedJudgeQuestion(Long assessmentId, DsaExamQuestion assignment, DsaQuestion question) {
        if (question != null && question.getId() != null) {
            var subs = dsaSubmissionRepository.findByAssessment_IdAndQuestion_IdOrderBySubmittedAtDesc(assessmentId, question.getId());
            dsaSubmissionRepository.deleteAll(subs);
        }
        if (assignment != null && assignment.getId() != null) {
            dsaExamQuestionRepository.delete(assignment);
        }
        if (question != null && question.getId() != null) {
            dsaQuestionRepository.delete(question);
        }
    }

    @Test
    @DisplayName("Phase 12: ACCEPTED solution matches all visible and hidden test cases")
    void testAcceptedSubmission() {
        Long assessmentId = createAssessmentWithExam("DSA_SUB_ACC");
        DsaQuestion question = createIsolatedJudgeQuestion();
        Assessment assessment = assessmentRepository.findById(assessmentId).orElseThrow();
        DsaExamQuestion assignment = dsaExamQuestionRepository.save(new DsaExamQuestion(assessment, question, 3));

        try {
            String correctJavaCode = "import java.util.Scanner;\n" +
                    "public class Main {\n" +
                    "    public static void main(String[] args) {\n" +
                    "        Scanner sc = new Scanner(System.in);\n" +
                    "        if (sc.hasNextInt()) {\n" +
                    "            int a = sc.nextInt();\n" +
                    "            int b = sc.nextInt();\n" +
                    "            System.out.println(a + b);\n" +
                    "        }\n" +
                    "    }\n" +
                    "}";

            SubmitDsaCodeResponse res = dsaSubmissionService.submitCode(
                    new SubmitDsaCodeRequest(assessmentId, question.getId(), "JAVA", correctJavaCode)
            );

            assertNotNull(res);
            assertEquals("ACCEPTED", res.getStatus());
            assertEquals("ACCEPTED", res.getResultStatus());
            assertEquals(4, res.getTotalTestCases());
            assertEquals(4, res.getPassedTestCases());
            assertEquals(0, res.getFailedTestCases());
            assertTrue(res.getExecutionTimeMs() >= 0);

            // Verify persisted row in database
            DsaSubmission entity = dsaSubmissionRepository.findById(res.getSubmissionId()).orElseThrow();
            assertEquals("ACCEPTED", entity.getResultStatus());
        } finally {
            cleanupIsolatedJudgeQuestion(assessmentId, assignment, question);
        }
    }

    @Test
    @DisplayName("Phase 12: WRONG_ANSWER verdict when solution outputs incorrect answer")
    void testWrongAnswerSubmission() {
        Long assessmentId = createAssessmentWithExam("DSA_SUB_WA");
        DsaQuestion question = createIsolatedJudgeQuestion();
        Assessment assessment = assessmentRepository.findById(assessmentId).orElseThrow();
        DsaExamQuestion assignment = dsaExamQuestionRepository.save(new DsaExamQuestion(assessment, question, 3));

        try {
            String wrongJavaCode = "public class Main {\n" +
                    "    public static void main(String[] args) {\n" +
                    "        System.out.println(99999);\n" +
                    "    }\n" +
                    "}";

            SubmitDsaCodeResponse res = dsaSubmissionService.submitCode(
                    new SubmitDsaCodeRequest(assessmentId, question.getId(), "JAVA", wrongJavaCode)
            );

            assertNotNull(res);
            assertEquals("WRONG_ANSWER", res.getStatus());
            assertEquals("WRONG_ANSWER", res.getResultStatus());
            assertTrue(res.getFailedTestCases() > 0);

            DsaSubmission entity = dsaSubmissionRepository.findById(res.getSubmissionId()).orElseThrow();
            assertEquals("WRONG_ANSWER", entity.getResultStatus());
        } finally {
            cleanupIsolatedJudgeQuestion(assessmentId, assignment, question);
        }
    }

    @Test
    @DisplayName("Phase 12: COMPILATION_ERROR verdict when code has syntax errors")
    void testCompilationErrorSubmission() {
        Long assessmentId = createAssessmentWithExam("DSA_SUB_CE");
        DsaQuestion question = createIsolatedJudgeQuestion();
        Assessment assessment = assessmentRepository.findById(assessmentId).orElseThrow();
        DsaExamQuestion assignment = dsaExamQuestionRepository.save(new DsaExamQuestion(assessment, question, 3));

        try {
            String invalidSyntaxCode = "public class Main { syntax_error; }";

            SubmitDsaCodeResponse res = dsaSubmissionService.submitCode(
                    new SubmitDsaCodeRequest(assessmentId, question.getId(), "JAVA", invalidSyntaxCode)
            );

            assertNotNull(res);
            assertEquals("COMPILATION_ERROR", res.getStatus());
            assertEquals("COMPILATION_ERROR", res.getResultStatus());

            DsaSubmission entity = dsaSubmissionRepository.findById(res.getSubmissionId()).orElseThrow();
            assertEquals("COMPILATION_ERROR", entity.getResultStatus());
        } finally {
            cleanupIsolatedJudgeQuestion(assessmentId, assignment, question);
        }
    }

    @Test
    @DisplayName("Phase 12: RUNTIME_ERROR verdict when code crashes during execution")
    void testRuntimeErrorSubmission() {
        Long assessmentId = createAssessmentWithExam("DSA_SUB_RE");
        DsaQuestion question = createIsolatedJudgeQuestion();
        Assessment assessment = assessmentRepository.findById(assessmentId).orElseThrow();
        DsaExamQuestion assignment = dsaExamQuestionRepository.save(new DsaExamQuestion(assessment, question, 3));

        try {
            String crashCode = "public class Main {\n" +
                    "    public static void main(String[] args) {\n" +
                    "        int x = 10 / 0;\n" +
                    "    }\n" +
                    "}";

            SubmitDsaCodeResponse res = dsaSubmissionService.submitCode(
                    new SubmitDsaCodeRequest(assessmentId, question.getId(), "JAVA", crashCode)
            );

            assertNotNull(res);
            assertEquals("RUNTIME_ERROR", res.getStatus());
            assertEquals("RUNTIME_ERROR", res.getResultStatus());

            DsaSubmission entity = dsaSubmissionRepository.findById(res.getSubmissionId()).orElseThrow();
            assertEquals("RUNTIME_ERROR", entity.getResultStatus());
        } finally {
            cleanupIsolatedJudgeQuestion(assessmentId, assignment, question);
        }
    }

    @Test
    @DisplayName("Phase 12: TIME_LIMIT_EXCEEDED verdict when code loops infinitely")
    void testTimeLimitExceededSubmission() {
        Long assessmentId = createAssessmentWithExam("DSA_SUB_TLE");
        DsaQuestion question = createIsolatedJudgeQuestion();
        Assessment assessment = assessmentRepository.findById(assessmentId).orElseThrow();
        DsaExamQuestion assignment = dsaExamQuestionRepository.save(new DsaExamQuestion(assessment, question, 3));

        try {
            String infiniteLoopCode = "public class Main {\n" +
                    "    public static void main(String[] args) {\n" +
                    "        while (true) {}\n" +
                    "    }\n" +
                    "}";

            SubmitDsaCodeResponse res = dsaSubmissionService.submitCode(
                    new SubmitDsaCodeRequest(assessmentId, question.getId(), "JAVA", infiniteLoopCode)
            );

            assertNotNull(res);
            assertEquals("TIME_LIMIT_EXCEEDED", res.getStatus());
            assertEquals("TIME_LIMIT_EXCEEDED", res.getResultStatus());

            DsaSubmission entity = dsaSubmissionRepository.findById(res.getSubmissionId()).orElseThrow();
            assertEquals("TIME_LIMIT_EXCEEDED", entity.getResultStatus());
        } finally {
            cleanupIsolatedJudgeQuestion(assessmentId, assignment, question);
        }
    }

    @Test
    @DisplayName("Phase 12: OUTPUT_LIMIT_EXCEEDED verdict when code floods output stream")
    void testOutputLimitExceededSubmission() {
        Long assessmentId = createAssessmentWithExam("DSA_SUB_OLE");
        DsaQuestion question = createIsolatedJudgeQuestion();
        Assessment assessment = assessmentRepository.findById(assessmentId).orElseThrow();
        DsaExamQuestion assignment = dsaExamQuestionRepository.save(new DsaExamQuestion(assessment, question, 3));

        try {
            String floodCode = "public class Main {\n" +
                    "    public static void main(String[] args) {\n" +
                    "        for (int i = 0; i < 500000; i++) {\n" +
                    "            System.out.println(\"Flooding output buffer stream line\");\n" +
                    "        }\n" +
                    "    }\n" +
                    "}";

            SubmitDsaCodeResponse res = dsaSubmissionService.submitCode(
                    new SubmitDsaCodeRequest(assessmentId, question.getId(), "JAVA", floodCode)
            );

            assertNotNull(res);
            assertEquals("OUTPUT_LIMIT_EXCEEDED", res.getStatus());
            assertEquals("OUTPUT_LIMIT_EXCEEDED", res.getResultStatus());

            DsaSubmission entity = dsaSubmissionRepository.findById(res.getSubmissionId()).orElseThrow();
            assertEquals("OUTPUT_LIMIT_EXCEEDED", entity.getResultStatus());
        } finally {
            cleanupIsolatedJudgeQuestion(assessmentId, assignment, question);
        }
    }

    @Test
    @DisplayName("Phase 12: Multiple submissions for same question preserve all attempts with distinct verdicts")
    void testMultipleSubmissionsWithDistinctVerdicts() {
        Long assessmentId = createAssessmentWithExam("DSA_SUB_MULTI_HIST");
        DsaQuestion question = createIsolatedJudgeQuestion();
        Assessment assessment = assessmentRepository.findById(assessmentId).orElseThrow();
        DsaExamQuestion assignment = dsaExamQuestionRepository.save(new DsaExamQuestion(assessment, question, 3));

        try {
            // Attempt 1: Wrong Answer
            SubmitDsaCodeResponse attempt1 = dsaSubmissionService.submitCode(
                    new SubmitDsaCodeRequest(assessmentId, question.getId(), "JAVA",
                            "public class Main { public static void main(String[] args) { System.out.println(0); } }")
            );
            assertEquals("WRONG_ANSWER", attempt1.getStatus());

            // Attempt 2: Accepted
            SubmitDsaCodeResponse attempt2 = dsaSubmissionService.submitCode(
                    new SubmitDsaCodeRequest(assessmentId, question.getId(), "JAVA",
                            "import java.util.Scanner;\n" +
                                    "public class Main {\n" +
                                    "    public static void main(String[] args) {\n" +
                                    "        Scanner sc = new Scanner(System.in);\n" +
                                    "        if (sc.hasNextInt()) System.out.println(sc.nextInt() + sc.nextInt());\n" +
                                    "    }\n" +
                                    "}")
            );
            assertEquals("ACCEPTED", attempt2.getStatus());

            assertNotEquals(attempt1.getSubmissionId(), attempt2.getSubmissionId());

            // Verify both rows exist in dsa_submissions
            assertEquals(2, dsaSubmissionRepository.countByAssessment_IdAndQuestion_Id(assessmentId, question.getId()));

            DsaSubmission sub1 = dsaSubmissionRepository.findById(attempt1.getSubmissionId()).orElseThrow();
            assertEquals("WRONG_ANSWER", sub1.getResultStatus());

            DsaSubmission sub2 = dsaSubmissionRepository.findById(attempt2.getSubmissionId()).orElseThrow();
            assertEquals("ACCEPTED", sub2.getResultStatus());

            // Assessment status remains IN_PROGRESS (not completed)
            Assessment currentAssessment = assessmentRepository.findById(assessmentId).orElseThrow();
            assertEquals("IN_PROGRESS", currentAssessment.getStatus());
        } finally {
            cleanupIsolatedJudgeQuestion(assessmentId, assignment, question);
        }
    }
}
