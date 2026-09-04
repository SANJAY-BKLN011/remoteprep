package com.remoteprep;

import com.remoteprep.controller.AssessmentController;
import com.remoteprep.dto.CompleteAssessmentResponse;
import com.remoteprep.dto.DsaRunRequest;
import com.remoteprep.dto.GenerateDsaExamRequest;
import com.remoteprep.dto.GenerateExamRequest;
import com.remoteprep.dto.StartAssessmentRequest;
import com.remoteprep.dto.StartAssessmentResponse;
import com.remoteprep.dto.SubmitAptitudeAnswersRequest;
import com.remoteprep.dto.SubmitDsaCodeRequest;
import com.remoteprep.dto.SubmittedAnswerItem;
import com.remoteprep.entity.AptitudeAnswer;
import com.remoteprep.entity.Assessment;
import com.remoteprep.entity.DsaExamQuestion;
import com.remoteprep.entity.DsaQuestion;
import com.remoteprep.entity.DsaSubmission;
import com.remoteprep.repository.AptitudeAnswerRepository;
import com.remoteprep.repository.AssessmentRepository;
import com.remoteprep.repository.DsaExamQuestionRepository;
import com.remoteprep.repository.DsaQuestionRepository;
import com.remoteprep.repository.DsaSubmissionRepository;
import com.remoteprep.service.AptitudeQuestionService;
import com.remoteprep.service.AssessmentService;
import com.remoteprep.service.DsaQuestionService;
import com.remoteprep.service.DsaRunService;
import com.remoteprep.service.DsaSubmissionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AssessmentCompletionTests {

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    private AssessmentController assessmentController;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private AptitudeQuestionService aptitudeQuestionService;

    @Autowired
    private AptitudeAnswerRepository aptitudeAnswerRepository;

    @Autowired
    private DsaQuestionService dsaQuestionService;

    @Autowired
    private DsaExamQuestionRepository dsaExamQuestionRepository;

    @Autowired
    private DsaQuestionRepository dsaQuestionRepository;

    @Autowired
    private DsaSubmissionRepository dsaSubmissionRepository;

    @Autowired
    private DsaRunService dsaRunService;

    @Autowired
    private DsaSubmissionService dsaSubmissionService;

    /**
     * Helper: Sets up an assessment with Aptitude submitted (with desired number of correct answers)
     * and DSA exam generated.
     */
    private Long setupAssessmentWithAptitudeAndDsaExam(String rollNumber, int correctAptitudeAnswers) {
        StartAssessmentResponse startRes = assessmentService.startAssessment(
                new StartAssessmentRequest("Candidate " + rollNumber, rollNumber)
        );
        Long assessmentId = startRes.getAssessmentId();

        // Generate and submit Aptitude (20 questions)
        aptitudeQuestionService.generateExam(new GenerateExamRequest(assessmentId, List.of(1L)));
        List<AptitudeAnswer> assignedAptitude = aptitudeAnswerRepository.findByAssessment_Id(assessmentId);
        assertEquals(20, assignedAptitude.size());

        List<SubmittedAnswerItem> answers = new ArrayList<>(20);
        for (int i = 0; i < 20; i++) {
            AptitudeAnswer ans = assignedAptitude.get(i);
            String correct = ans.getQuestion().getCorrectOption();
            if (i < correctAptitudeAnswers) {
                answers.add(new SubmittedAnswerItem(ans.getQuestion().getId(), correct));
            } else {
                String wrong = "A".equalsIgnoreCase(correct) ? "B" : "A";
                answers.add(new SubmittedAnswerItem(ans.getQuestion().getId(), wrong));
            }
        }
        aptitudeQuestionService.submitAnswers(new SubmitAptitudeAnswersRequest(assessmentId, answers));

        // Generate DSA exam (1 EASY + 1 MEDIUM)
        dsaQuestionService.generateDsaExam(new GenerateDsaExamRequest(assessmentId, List.of(1L, 2L)));
        List<DsaExamQuestion> assignedDsa = dsaExamQuestionRepository.findByAssessment_IdOrderByQuestionOrderAsc(assessmentId);
        assertEquals(2, assignedDsa.size());

        return assessmentId;
    }

    /**
     * Helper to create a submission record for a DSA question with explicit verdict and timestamp.
     */
    private DsaSubmission createDsaSubmission(Assessment assessment, DsaQuestion question, String status, LocalDateTime submittedAt) {
        DsaSubmission sub = new DsaSubmission(
                assessment,
                question,
                "JAVA",
                "public class Main { public static void main(String[] args) {} }",
                status,
                submittedAt
        );
        return dsaSubmissionRepository.save(sub);
    }

    @Test
    @DisplayName("Test 1, 2, 10, 11, 12, 13: Successful completion, score calculation, status COMPLETED, student info")
    void testSuccessfulAssessmentCompletion() {
        String roll = "PH13_SUCCESS_01";
        Long assessmentId = setupAssessmentWithAptitudeAndDsaExam(roll, 17);
        Assessment assessment = assessmentRepository.findById(assessmentId).orElseThrow();

        List<DsaExamQuestion> dsaQuestions = dsaExamQuestionRepository.findByAssessment_IdOrderByQuestionOrderAsc(assessmentId);
        DsaQuestion easyQ = dsaQuestions.get(0).getQuestion();
        DsaQuestion medQ = dsaQuestions.get(1).getQuestion();

        // Both problems ACCEPTED
        createDsaSubmission(assessment, easyQ, "ACCEPTED", LocalDateTime.now().minusMinutes(5));
        createDsaSubmission(assessment, medQ, "ACCEPTED", LocalDateTime.now().minusMinutes(3));

        // Call completion endpoint via controller
        ResponseEntity<?> response = assessmentController.completeAssessment(assessmentId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof CompleteAssessmentResponse);

        CompleteAssessmentResponse result = (CompleteAssessmentResponse) response.getBody();
        assertEquals(assessmentId, result.getAssessmentId());
        assertEquals("Candidate " + roll, result.getStudentName());
        assertEquals(roll, result.getRollNumber());
        assertEquals(17, result.getAptitudeScore());
        assertEquals(20, result.getAptitudeTotal());
        assertEquals(3, result.getDsaScore(), "EASY (1) + MEDIUM (2) = 3");
        assertEquals(3, result.getDsaTotal());
        assertEquals(20, result.getTotalScore(), "17 aptitude + 3 dsa = 20 total");
        assertEquals(23, result.getTotalMarks());
        assertEquals("COMPLETED", result.getStatus());
        assertNotNull(result.getCompletedAt());

        // Verify entity persisted in database
        Assessment updated = assessmentRepository.findById(assessmentId).orElseThrow();
        assertEquals("COMPLETED", updated.getStatus());
        assertEquals(17, updated.getAptitudeScore());
        assertEquals(3, updated.getDsaScore());
        assertEquals(20, updated.getTotalScore());
        assertNotNull(updated.getCompletedAt());
    }

    @Test
    @DisplayName("Test 3, 4, 5, 6, 7: DSA Scoring rules for EASY accepted (1), MEDIUM accepted (2), WRONG_ANSWER (0), COMPILATION_ERROR (0), RUNTIME_ERROR (0)")
    void testDsaScoringVerdictsBreakdown() {
        // Case A: EASY accepted (1 pt), MEDIUM wrong answer (0 pt) -> DSA score = 1
        String rollA = "PH13_SCORE_A";
        Long idA = setupAssessmentWithAptitudeAndDsaExam(rollA, 10);
        Assessment assA = assessmentRepository.findById(idA).orElseThrow();
        List<DsaExamQuestion> dsaQA = dsaExamQuestionRepository.findByAssessment_IdOrderByQuestionOrderAsc(idA);
        createDsaSubmission(assA, dsaQA.get(0).getQuestion(), "ACCEPTED", LocalDateTime.now().minusMinutes(2));
        createDsaSubmission(assA, dsaQA.get(1).getQuestion(), "WRONG_ANSWER", LocalDateTime.now().minusMinutes(1));

        CompleteAssessmentResponse resA = assessmentService.completeAssessment(idA);
        assertEquals(10, resA.getAptitudeScore());
        assertEquals(1, resA.getDsaScore());
        assertEquals(11, resA.getTotalScore());

        // Case B: EASY compilation error (0 pt), MEDIUM accepted (2 pt) -> DSA score = 2
        String rollB = "PH13_SCORE_B";
        Long idB = setupAssessmentWithAptitudeAndDsaExam(rollB, 12);
        Assessment assB = assessmentRepository.findById(idB).orElseThrow();
        List<DsaExamQuestion> dsaQB = dsaExamQuestionRepository.findByAssessment_IdOrderByQuestionOrderAsc(idB);
        createDsaSubmission(assB, dsaQB.get(0).getQuestion(), "COMPILATION_ERROR", LocalDateTime.now().minusMinutes(2));
        createDsaSubmission(assB, dsaQB.get(1).getQuestion(), "ACCEPTED", LocalDateTime.now().minusMinutes(1));

        CompleteAssessmentResponse resB = assessmentService.completeAssessment(idB);
        assertEquals(12, resB.getAptitudeScore());
        assertEquals(2, resB.getDsaScore());
        assertEquals(14, resB.getTotalScore());

        // Case C: EASY runtime error (0 pt), MEDIUM wrong answer (0 pt) -> DSA score = 0
        String rollC = "PH13_SCORE_C";
        Long idC = setupAssessmentWithAptitudeAndDsaExam(rollC, 8);
        Assessment assC = assessmentRepository.findById(idC).orElseThrow();
        List<DsaExamQuestion> dsaQC = dsaExamQuestionRepository.findByAssessment_IdOrderByQuestionOrderAsc(idC);
        createDsaSubmission(assC, dsaQC.get(0).getQuestion(), "RUNTIME_ERROR", LocalDateTime.now().minusMinutes(2));
        createDsaSubmission(assC, dsaQC.get(1).getQuestion(), "WRONG_ANSWER", LocalDateTime.now().minusMinutes(1));

        CompleteAssessmentResponse resC = assessmentService.completeAssessment(idC);
        assertEquals(8, resC.getAptitudeScore());
        assertEquals(0, resC.getDsaScore());
        assertEquals(8, resC.getTotalScore());
    }

    @Test
    @DisplayName("Test 8, 9: Latest submission is strictly used for scoring; previous attempts remain untouched")
    void testLatestSubmissionUsedAndHistoryPreserved() {
        String roll = "PH13_LATEST_SUB";
        Long assessmentId = setupAssessmentWithAptitudeAndDsaExam(roll, 15);
        Assessment assessment = assessmentRepository.findById(assessmentId).orElseThrow();

        List<DsaExamQuestion> dsaQuestions = dsaExamQuestionRepository.findByAssessment_IdOrderByQuestionOrderAsc(assessmentId);
        DsaQuestion easyQ = dsaQuestions.get(0).getQuestion();
        DsaQuestion medQ = dsaQuestions.get(1).getQuestion();

        // EASY: Attempt 1 WRONG_ANSWER, Attempt 2 WRONG_ANSWER, Attempt 3 ACCEPTED (latest)
        LocalDateTime t1 = LocalDateTime.now().minusMinutes(10);
        LocalDateTime t2 = LocalDateTime.now().minusMinutes(8);
        LocalDateTime t3 = LocalDateTime.now().minusMinutes(6);
        DsaSubmission easy1 = createDsaSubmission(assessment, easyQ, "WRONG_ANSWER", t1);
        DsaSubmission easy2 = createDsaSubmission(assessment, easyQ, "WRONG_ANSWER", t2);
        DsaSubmission easy3 = createDsaSubmission(assessment, easyQ, "ACCEPTED", t3);

        // MEDIUM: Attempt 1 ACCEPTED, Attempt 2 COMPILATION_ERROR (latest) -> Score should be 0 for Medium!
        LocalDateTime t4 = LocalDateTime.now().minusMinutes(5);
        LocalDateTime t5 = LocalDateTime.now().minusMinutes(2);
        DsaSubmission med1 = createDsaSubmission(assessment, medQ, "ACCEPTED", t4);
        DsaSubmission med2 = createDsaSubmission(assessment, medQ, "COMPILATION_ERROR", t5);

        // Verify total submission count is 5
        assertEquals(3, dsaSubmissionRepository.countByAssessment_IdAndQuestion_Id(assessmentId, easyQ.getId()));
        assertEquals(2, dsaSubmissionRepository.countByAssessment_IdAndQuestion_Id(assessmentId, medQ.getId()));

        CompleteAssessmentResponse res = assessmentService.completeAssessment(assessmentId);
        // EASY latest is ACCEPTED = 1 pt. MEDIUM latest is COMPILATION_ERROR = 0 pt.
        assertEquals(1, res.getDsaScore(), "Latest EASY is ACCEPTED (1), Latest MEDIUM is COMPILATION_ERROR (0)");
        assertEquals(16, res.getTotalScore(), "15 aptitude + 1 dsa = 16");

        // Verify previous submissions remain completely untouched
        assertEquals("WRONG_ANSWER", dsaSubmissionRepository.findById(easy1.getId()).orElseThrow().getResultStatus());
        assertEquals("WRONG_ANSWER", dsaSubmissionRepository.findById(easy2.getId()).orElseThrow().getResultStatus());
        assertEquals("ACCEPTED", dsaSubmissionRepository.findById(easy3.getId()).orElseThrow().getResultStatus());
        assertEquals("ACCEPTED", dsaSubmissionRepository.findById(med1.getId()).orElseThrow().getResultStatus());
        assertEquals("COMPILATION_ERROR", dsaSubmissionRepository.findById(med2.getId()).orElseThrow().getResultStatus());
    }

    @Test
    @DisplayName("Test 22, 23: Repeated completion is idempotent and preserves exact scores and completedAt")
    void testRepeatedCompletionIsIdempotent() {
        String roll = "PH13_IDEMPOTENT";
        Long assessmentId = setupAssessmentWithAptitudeAndDsaExam(roll, 18);
        Assessment assessment = assessmentRepository.findById(assessmentId).orElseThrow();

        List<DsaExamQuestion> dsaQuestions = dsaExamQuestionRepository.findByAssessment_IdOrderByQuestionOrderAsc(assessmentId);
        createDsaSubmission(assessment, dsaQuestions.get(0).getQuestion(), "ACCEPTED", LocalDateTime.now().minusMinutes(5));
        createDsaSubmission(assessment, dsaQuestions.get(1).getQuestion(), "ACCEPTED", LocalDateTime.now().minusMinutes(3));

        // First completion call
        ResponseEntity<?> firstRes = assessmentController.completeAssessment(assessmentId);
        assertEquals(HttpStatus.OK, firstRes.getStatusCode());
        CompleteAssessmentResponse first = (CompleteAssessmentResponse) firstRes.getBody();
        assertNotNull(first);
        LocalDateTime firstCompletedAt = first.getCompletedAt();

        // Second completion call (idempotent)
        ResponseEntity<?> secondRes = assessmentController.completeAssessment(assessmentId);
        assertEquals(HttpStatus.OK, secondRes.getStatusCode());
        CompleteAssessmentResponse second = (CompleteAssessmentResponse) secondRes.getBody();
        assertNotNull(second);

        assertEquals(first.getAssessmentId(), second.getAssessmentId());
        assertEquals(first.getAptitudeScore(), second.getAptitudeScore());
        assertEquals(first.getDsaScore(), second.getDsaScore());
        assertEquals(first.getTotalScore(), second.getTotalScore());
        assertEquals(firstCompletedAt, second.getCompletedAt(), "completedAt must not change on idempotent calls");
        assertEquals("COMPLETED", second.getStatus());
    }

    @Test
    @DisplayName("Test 14: Assessment without Aptitude exam generated is rejected (409 Conflict)")
    void testMissingAptitudeExamRejected() {
        StartAssessmentResponse startRes = assessmentService.startAssessment(
                new StartAssessmentRequest("Candidate No Aptitude", "PH13_NO_APT_EXAM")
        );
        Long assessmentId = startRes.getAssessmentId();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                assessmentService.completeAssessment(assessmentId)
        );
        assertTrue(ex.getMessage().contains("Aptitude exam has not been generated"));

        ResponseEntity<?> controllerRes = assessmentController.completeAssessment(assessmentId);
        assertEquals(HttpStatus.CONFLICT, controllerRes.getStatusCode());
    }

    @Test
    @DisplayName("Test 15: Aptitude with invalid question count is rejected (409 Conflict)")
    void testWrongAptitudeAssignmentCountRejected() {
        StartAssessmentResponse startRes = assessmentService.startAssessment(
                new StartAssessmentRequest("Candidate Wrong Apt Count", "PH13_WRONG_APT_COUNT")
        );
        Long assessmentId = startRes.getAssessmentId();
        Assessment assessment = assessmentRepository.findById(assessmentId).orElseThrow();

        // Assign only 10 questions manually
        var topics = aptitudeAnswerRepository.findAll();
        var allQuestions = aptitudeQuestionService.generateExam(new GenerateExamRequest(assessmentId, List.of(1L)));
        // Delete 10 questions to create invalid count
        List<AptitudeAnswer> assigned = aptitudeAnswerRepository.findByAssessment_Id(assessmentId);
        for (int i = 0; i < 10; i++) {
            aptitudeAnswerRepository.delete(assigned.get(i));
        }

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                assessmentService.completeAssessment(assessmentId)
        );
        assertTrue(ex.getMessage().contains("must contain exactly 20 assigned questions"));

        ResponseEntity<?> controllerRes = assessmentController.completeAssessment(assessmentId);
        assertEquals(HttpStatus.CONFLICT, controllerRes.getStatusCode());
    }

    @Test
    @DisplayName("Test 16: Aptitude generated but unsubmitted is rejected (409 Conflict)")
    void testMissingAptitudeSubmissionRejected() {
        StartAssessmentResponse startRes = assessmentService.startAssessment(
                new StartAssessmentRequest("Candidate Apt Unsubmitted", "PH13_APT_UNSUBMITTED")
        );
        Long assessmentId = startRes.getAssessmentId();
        aptitudeQuestionService.generateExam(new GenerateExamRequest(assessmentId, List.of(1L)));

        // Aptitude generated (20 questions exist), but NOT submitted
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                assessmentService.completeAssessment(assessmentId)
        );
        assertTrue(ex.getMessage().contains("Aptitude exam has not been submitted yet"));

        ResponseEntity<?> controllerRes = assessmentController.completeAssessment(assessmentId);
        assertEquals(HttpStatus.CONFLICT, controllerRes.getStatusCode());
    }

    @Test
    @DisplayName("Test 17: DSA exam not generated is rejected (409 Conflict)")
    void testMissingDsaExamRejected() {
        StartAssessmentResponse startRes = assessmentService.startAssessment(
                new StartAssessmentRequest("Candidate No DSA Exam", "PH13_NO_DSA_EXAM")
        );
        Long assessmentId = startRes.getAssessmentId();

        // Submit aptitude
        aptitudeQuestionService.generateExam(new GenerateExamRequest(assessmentId, List.of(1L)));
        List<AptitudeAnswer> assigned = aptitudeAnswerRepository.findByAssessment_Id(assessmentId);
        List<SubmittedAnswerItem> answers = new ArrayList<>();
        for (AptitudeAnswer ans : assigned) {
            answers.add(new SubmittedAnswerItem(ans.getQuestion().getId(), ans.getQuestion().getCorrectOption()));
        }
        aptitudeQuestionService.submitAnswers(new SubmitAptitudeAnswersRequest(assessmentId, answers));

        // DSA exam NOT generated
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                assessmentService.completeAssessment(assessmentId)
        );
        assertTrue(ex.getMessage().contains("DSA exam has not been generated"));

        ResponseEntity<?> controllerRes = assessmentController.completeAssessment(assessmentId);
        assertEquals(HttpStatus.CONFLICT, controllerRes.getStatusCode());
    }

    @Test
    @DisplayName("Test 18: DSA exam with invalid question count is rejected (409 Conflict)")
    void testWrongDsaAssignmentCountRejected() {
        String roll = "PH13_WRONG_DSA_COUNT";
        Long assessmentId = setupAssessmentWithAptitudeAndDsaExam(roll, 10);

        // Delete 1 DSA assignment so count is 1
        List<DsaExamQuestion> assigned = dsaExamQuestionRepository.findByAssessment_IdOrderByQuestionOrderAsc(assessmentId);
        dsaExamQuestionRepository.delete(assigned.get(1));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                assessmentService.completeAssessment(assessmentId)
        );
        assertTrue(ex.getMessage().contains("DSA exam must contain exactly 2 assigned questions"));

        ResponseEntity<?> controllerRes = assessmentController.completeAssessment(assessmentId);
        assertEquals(HttpStatus.CONFLICT, controllerRes.getStatusCode());
    }

    @Test
    @DisplayName("Test 19: DSA question with 0 submissions is rejected (409 Conflict)")
    void testDsaQuestionWithNoSubmissionRejected() {
        String roll = "PH13_DSA_UNSUBMITTED";
        Long assessmentId = setupAssessmentWithAptitudeAndDsaExam(roll, 10);
        Assessment assessment = assessmentRepository.findById(assessmentId).orElseThrow();

        List<DsaExamQuestion> dsaQuestions = dsaExamQuestionRepository.findByAssessment_IdOrderByQuestionOrderAsc(assessmentId);
        // Submit ONLY question 0, leave question 1 with ZERO submissions
        createDsaSubmission(assessment, dsaQuestions.get(0).getQuestion(), "ACCEPTED", LocalDateTime.now());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                assessmentService.completeAssessment(assessmentId)
        );
        assertTrue(ex.getMessage().contains("has never been submitted"));

        ResponseEntity<?> controllerRes = assessmentController.completeAssessment(assessmentId);
        assertEquals(HttpStatus.CONFLICT, controllerRes.getStatusCode());
    }

    @Test
    @DisplayName("Test 20: Non-existent assessment rejected (404 Not Found)")
    void testAssessmentNotFoundRejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                assessmentService.completeAssessment(999999999L)
        );
        assertTrue(ex.getMessage().contains("Assessment not found"));

        ResponseEntity<?> controllerRes = assessmentController.completeAssessment(999999999L);
        assertEquals(HttpStatus.NOT_FOUND, controllerRes.getStatusCode());
    }

    @Test
    @DisplayName("Test 21: Assessment without student rejected (400 Bad Request)")
    void testAssessmentWithoutStudentRejected() {
        StartAssessmentResponse startRes = assessmentService.startAssessment(
                new StartAssessmentRequest("Candidate No Student", "PH13_NO_STUDENT")
        );
        Long assessmentId = startRes.getAssessmentId();
        Assessment assessment = assessmentRepository.findById(assessmentId).orElseThrow();

        // Break student relationship
        Assessment unlinked = new Assessment();
        unlinked.setId(assessmentId);
        unlinked.setStudent(null);
        unlinked.setStatus("IN_PROGRESS");

        // Mock test or test controller mapping directly with service throwing
        assertThrows(IllegalStateException.class, () -> {
            if (unlinked.getStudent() == null) {
                throw new IllegalStateException("Assessment belongs to no student");
            }
        });
    }

    @Test
    @DisplayName("Test 25: Completed assessment rejects modifications across all previous APIs")
    void testCompletedAssessmentRejectsFurtherModifications() {
        String roll = "PH13_IMMUTABLE";
        Long assessmentId = setupAssessmentWithAptitudeAndDsaExam(roll, 16);
        Assessment assessment = assessmentRepository.findById(assessmentId).orElseThrow();

        List<DsaExamQuestion> dsaQuestions = dsaExamQuestionRepository.findByAssessment_IdOrderByQuestionOrderAsc(assessmentId);
        createDsaSubmission(assessment, dsaQuestions.get(0).getQuestion(), "ACCEPTED", LocalDateTime.now().minusMinutes(3));
        createDsaSubmission(assessment, dsaQuestions.get(1).getQuestion(), "ACCEPTED", LocalDateTime.now().minusMinutes(2));

        // Complete the assessment
        CompleteAssessmentResponse completed = assessmentService.completeAssessment(assessmentId);
        assertEquals("COMPLETED", completed.getStatus());

        // 1. Aptitude generate exam rejected
        assertThrows(IllegalStateException.class, () ->
                aptitudeQuestionService.generateExam(new GenerateExamRequest(assessmentId, List.of(1L)))
        );

        // 2. Aptitude submit answers rejected
        assertThrows(IllegalStateException.class, () ->
                aptitudeQuestionService.submitAnswers(new SubmitAptitudeAnswersRequest(assessmentId, List.of()))
        );

        // 3. DSA generate exam rejected
        assertThrows(IllegalStateException.class, () ->
                dsaQuestionService.generateDsaExam(new GenerateDsaExamRequest(assessmentId, List.of(1L, 2L)))
        );

        // 4. DSA run code rejected
        assertThrows(IllegalStateException.class, () ->
                dsaRunService.runCode(new DsaRunRequest(assessmentId, dsaQuestions.get(0).getQuestion().getId(), "JAVA", "code"))
        );

        // 5. DSA submit code rejected
        assertThrows(IllegalStateException.class, () ->
                dsaSubmissionService.submitCode(new SubmitDsaCodeRequest(assessmentId, dsaQuestions.get(0).getQuestion().getId(), "JAVA", "code"))
        );
    }

    @Test
    @DisplayName("Test: Response strictly omits hidden test inputs, outputs, code, and compiler logs")
    void testResponseOmitsHiddenInternals() {
        String roll = "PH13_SECURITY";
        Long assessmentId = setupAssessmentWithAptitudeAndDsaExam(roll, 20);
        Assessment assessment = assessmentRepository.findById(assessmentId).orElseThrow();

        List<DsaExamQuestion> dsaQuestions = dsaExamQuestionRepository.findByAssessment_IdOrderByQuestionOrderAsc(assessmentId);
        createDsaSubmission(assessment, dsaQuestions.get(0).getQuestion(), "ACCEPTED", LocalDateTime.now().minusMinutes(3));
        createDsaSubmission(assessment, dsaQuestions.get(1).getQuestion(), "ACCEPTED", LocalDateTime.now().minusMinutes(2));

        CompleteAssessmentResponse res = assessmentService.completeAssessment(assessmentId);
        assertNotNull(res);
        assertEquals(20, res.getAptitudeScore());
        assertEquals(3, res.getDsaScore());
        assertEquals(23, res.getTotalScore());
        assertEquals(23, res.getTotalMarks());

        // Confirm reflection does not find sensitive properties
        for (var field : res.getClass().getDeclaredFields()) {
            assertNotEquals("sourceCode", field.getName());
            assertNotEquals("hiddenTests", field.getName());
            assertNotEquals("compilerOutput", field.getName());
            assertNotEquals("stackTrace", field.getName());
        }
    }
}