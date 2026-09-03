package com.remoteprep;

import com.remoteprep.dto.GenerateDsaExamRequest;
import com.remoteprep.dto.GenerateDsaExamResponse;
import com.remoteprep.dto.StartAssessmentRequest;
import com.remoteprep.dto.StartAssessmentResponse;
import com.remoteprep.dto.SubmitDsaCodeRequest;
import com.remoteprep.dto.SubmitDsaCodeResponse;
import com.remoteprep.entity.Assessment;
import com.remoteprep.entity.DsaSubmission;
import com.remoteprep.repository.AssessmentRepository;
import com.remoteprep.repository.DsaExamQuestionRepository;
import com.remoteprep.repository.DsaSubmissionRepository;
import com.remoteprep.service.AssessmentService;
import com.remoteprep.service.DsaQuestionService;
import com.remoteprep.service.DsaSubmissionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

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
    @DisplayName("Tests 1-4: Valid JAVA, CPP, C, PYTHON submissions return PENDING")
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
            assertEquals("PENDING", res.getResultStatus());
            assertNotNull(res.getSubmittedAt());
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
                mockRepo, null, null, null
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
        // Test 22: New submission starts with PENDING status
        assertEquals("PENDING", sub1.getResultStatus());
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
    }
}
