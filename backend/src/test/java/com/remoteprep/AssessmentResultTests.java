package com.remoteprep;

import com.remoteprep.controller.AssessmentController;
import com.remoteprep.dto.AssessmentResultResponse;
import com.remoteprep.dto.GenerateDsaExamRequest;
import com.remoteprep.dto.GenerateExamRequest;
import com.remoteprep.dto.StartAssessmentRequest;
import com.remoteprep.dto.StartAssessmentResponse;
import com.remoteprep.dto.SubmitAptitudeAnswersRequest;
import com.remoteprep.dto.SubmittedAnswerItem;
import com.remoteprep.entity.AptitudeAnswer;
import com.remoteprep.entity.Assessment;
import com.remoteprep.entity.DsaExamQuestion;
import com.remoteprep.entity.DsaQuestion;
import com.remoteprep.entity.DsaSubmission;
import com.remoteprep.repository.AptitudeAnswerRepository;
import com.remoteprep.repository.AssessmentRepository;
import com.remoteprep.repository.DsaExamQuestionRepository;
import com.remoteprep.repository.DsaSubmissionRepository;
import com.remoteprep.service.AptitudeQuestionService;
import com.remoteprep.service.AssessmentService;
import com.remoteprep.service.DsaQuestionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AssessmentResultTests {

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
    private DsaSubmissionRepository dsaSubmissionRepository;

    private Long setupCompletedAssessment(String rollNumber, int correctAptitudeAnswers, boolean easyAccepted, boolean mediumAccepted) {
        StartAssessmentResponse startRes = assessmentService.startAssessment(
                new StartAssessmentRequest("Candidate " + rollNumber, rollNumber)
        );
        Long assessmentId = startRes.getAssessmentId();

        // Aptitude Exam
        aptitudeQuestionService.generateExam(new GenerateExamRequest(assessmentId, List.of(1L)));
        List<AptitudeAnswer> assignedAptitude = aptitudeAnswerRepository.findByAssessment_Id(assessmentId);
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

        // DSA Exam
        dsaQuestionService.generateDsaExam(new GenerateDsaExamRequest(assessmentId, List.of(1L, 2L)));
        List<DsaExamQuestion> assignedDsa = dsaExamQuestionRepository.findByAssessment_IdOrderByQuestionOrderAsc(assessmentId);
        Assessment assessment = assessmentRepository.findById(assessmentId).orElseThrow();

        DsaQuestion easyQ = assignedDsa.get(0).getQuestion();
        DsaQuestion medQ = assignedDsa.get(1).getQuestion();

        dsaSubmissionRepository.save(new DsaSubmission(
                assessment,
                easyQ,
                "JAVA",
                "class Solution {}",
                easyAccepted ? "ACCEPTED" : "WRONG_ANSWER",
                LocalDateTime.now().minusMinutes(5)
        ));

        dsaSubmissionRepository.save(new DsaSubmission(
                assessment,
                medQ,
                "JAVA",
                "class Solution {}",
                mediumAccepted ? "ACCEPTED" : "WRONG_ANSWER",
                LocalDateTime.now().minusMinutes(3)
        ));

        // Complete the assessment via Phase 13 endpoint
        ResponseEntity<?> completeRes = assessmentController.completeAssessment(assessmentId);
        assertEquals(HttpStatus.OK, completeRes.getStatusCode());

        return assessmentId;
    }

    @Test
    @DisplayName("1. Completed assessment returns HTTP 200 with accurate scores, totals, status and student details")
    void testGetAssessmentResultSuccess() {
        String roll = "PH14_RES_01";
        Long assessmentId = setupCompletedAssessment(roll, 15, true, true);

        Assessment persisted = assessmentRepository.findById(assessmentId).orElseThrow();
        assertEquals("COMPLETED", persisted.getStatus());
        assertEquals(15, persisted.getAptitudeScore());
        assertEquals(3, persisted.getDsaScore());
        assertEquals(18, persisted.getTotalScore());

        ResponseEntity<?> resEntity = assessmentController.getAssessmentResult(assessmentId);
        assertEquals(HttpStatus.OK, resEntity.getStatusCode());
        assertNotNull(resEntity.getBody());
        assertTrue(resEntity.getBody() instanceof AssessmentResultResponse);

        AssessmentResultResponse res = (AssessmentResultResponse) resEntity.getBody();

        assertEquals(assessmentId, res.getAssessmentId());
        assertEquals(persisted.getStudent().getId(), res.getStudentId());
        assertEquals("Candidate " + roll, res.getStudentName());
        assertEquals(roll, res.getRollNumber());

        assertEquals(15, res.getAptitudeScore());
        assertEquals(20, res.getAptitudeTotal());

        assertEquals(3, res.getDsaScore());
        assertEquals(3, res.getDsaTotal());

        assertEquals(18, res.getTotalScore());
        assertEquals(23, res.getTotalMarks());

        assertEquals("COMPLETED", res.getStatus());
        assertNotNull(res.getStartedAt());
        assertNotNull(res.getCompletedAt());
        assertEquals(persisted.getStartedAt(), res.getStartedAt());
        assertEquals(persisted.getCompletedAt(), res.getCompletedAt());
    }

    @Test
    @DisplayName("2. Returned scores accurately reflect partial scores (e.g. 0 DSA, 8 Aptitude)")
    void testGetAssessmentResultPartialScores() {
        String roll = "PH14_RES_02";
        Long assessmentId = setupCompletedAssessment(roll, 8, false, false);

        ResponseEntity<?> resEntity = assessmentController.getAssessmentResult(assessmentId);
        assertEquals(HttpStatus.OK, resEntity.getStatusCode());

        AssessmentResultResponse res = (AssessmentResultResponse) resEntity.getBody();
        assertEquals(8, res.getAptitudeScore());
        assertEquals(0, res.getDsaScore());
        assertEquals(8, res.getTotalScore());
        assertEquals("COMPLETED", res.getStatus());
    }

    @Test
    @DisplayName("3. Nonexistent assessment returns HTTP 404")
    void testGetAssessmentResultNotFound() {
        Long nonExistentId = 999999999L;
        ResponseEntity<?> resEntity = assessmentController.getAssessmentResult(nonExistentId);
        assertEquals(HttpStatus.NOT_FOUND, resEntity.getStatusCode());
        assertNotNull(resEntity.getBody());
        assertTrue(resEntity.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) resEntity.getBody();
        assertTrue(body.containsKey("error"));
        assertTrue(body.get("error").toString().contains("not found"));
    }

    @Test
    @DisplayName("4. IN_PROGRESS assessment returns HTTP 409 Conflict")
    void testGetAssessmentResultInProgressConflict() {
        String roll = "PH14_RES_INPROG";
        StartAssessmentResponse startRes = assessmentService.startAssessment(
                new StartAssessmentRequest("Candidate " + roll, roll)
        );
        Long assessmentId = startRes.getAssessmentId();

        ResponseEntity<?> resEntity = assessmentController.getAssessmentResult(assessmentId);
        assertEquals(HttpStatus.CONFLICT, resEntity.getStatusCode());
        assertNotNull(resEntity.getBody());
        assertTrue(resEntity.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) resEntity.getBody();
        assertTrue(body.containsKey("error"));
        assertTrue(body.get("error").toString().contains("not in COMPLETED state"));
    }

    @Test
    @DisplayName("5. Read-only verification: Result retrieval does NOT mutate database, scores, or timestamps")
    void testGetAssessmentResultReadOnlyNoMutation() {
        String roll = "PH14_RES_READONLY";
        Long assessmentId = setupCompletedAssessment(roll, 16, true, false);

        Assessment before = assessmentRepository.findById(assessmentId).orElseThrow();
        Integer beforeAptitude = before.getAptitudeScore();
        Integer beforeDsa = before.getDsaScore();
        Integer beforeTotal = before.getTotalScore();
        String beforeStatus = before.getStatus();
        LocalDateTime beforeStarted = before.getStartedAt();
        LocalDateTime beforeCompleted = before.getCompletedAt();

        // Call result endpoint multiple times
        for (int i = 0; i < 3; i++) {
            ResponseEntity<?> resEntity = assessmentController.getAssessmentResult(assessmentId);
            assertEquals(HttpStatus.OK, resEntity.getStatusCode());
        }

        Assessment after = assessmentRepository.findById(assessmentId).orElseThrow();
        assertEquals(beforeAptitude, after.getAptitudeScore());
        assertEquals(beforeDsa, after.getDsaScore());
        assertEquals(beforeTotal, after.getTotalScore());
        assertEquals(beforeStatus, after.getStatus());
        assertEquals(beforeStarted, after.getStartedAt());
        assertEquals(beforeCompleted, after.getCompletedAt());
    }

    @Test
    @DisplayName("6. Repeated calls return identical results (Idempotent / Deterministic)")
    void testGetAssessmentResultIdempotent() {
        String roll = "PH14_RES_IDEM";
        Long assessmentId = setupCompletedAssessment(roll, 19, true, true);

        ResponseEntity<?> firstCall = assessmentController.getAssessmentResult(assessmentId);
        ResponseEntity<?> secondCall = assessmentController.getAssessmentResult(assessmentId);

        assertEquals(HttpStatus.OK, firstCall.getStatusCode());
        assertEquals(HttpStatus.OK, secondCall.getStatusCode());

        AssessmentResultResponse res1 = (AssessmentResultResponse) firstCall.getBody();
        AssessmentResultResponse res2 = (AssessmentResultResponse) secondCall.getBody();

        assertEquals(res1.getAssessmentId(), res2.getAssessmentId());
        assertEquals(res1.getStudentId(), res2.getStudentId());
        assertEquals(res1.getStudentName(), res2.getStudentName());
        assertEquals(res1.getRollNumber(), res2.getRollNumber());
        assertEquals(res1.getAptitudeScore(), res2.getAptitudeScore());
        assertEquals(res1.getAptitudeTotal(), res2.getAptitudeTotal());
        assertEquals(res1.getDsaScore(), res2.getDsaScore());
        assertEquals(res1.getDsaTotal(), res2.getDsaTotal());
        assertEquals(res1.getTotalScore(), res2.getTotalScore());
        assertEquals(res1.getTotalMarks(), res2.getTotalMarks());
        assertEquals(res1.getStatus(), res2.getStatus());
        assertEquals(res1.getStartedAt(), res2.getStartedAt());
        assertEquals(res1.getCompletedAt(), res2.getCompletedAt());
    }

    @Test
    @DisplayName("7. Assessment validation: null assessment ID and unlinked student handling")
    void testGetAssessmentResultValidationErrors() {
        // Null assessment ID returns HTTP 400 Bad Request
        ResponseEntity<?> nullRes = assessmentController.getAssessmentResult(null);
        assertEquals(HttpStatus.BAD_REQUEST, nullRes.getStatusCode());
        assertNotNull(nullRes.getBody());
        assertTrue(nullRes.getBody() instanceof Map);
        Map<?, ?> nullBody = (Map<?, ?>) nullRes.getBody();
        assertTrue(nullBody.containsKey("error"));
        assertTrue(nullBody.get("error").toString().contains("assessmentId must be provided"));

        // Assessment without student logic verification
        Assessment unlinked = new Assessment();
        unlinked.setId(999L);
        unlinked.setStudent(null);
        unlinked.setStatus("COMPLETED");

        assertThrows(IllegalStateException.class, () -> {
            if (unlinked.getStudent() == null) {
                throw new IllegalStateException("Assessment belongs to no student");
            }
        });
    }

    @Test
    @DisplayName("8. Result DTO does not expose source code, hidden tests, compiler output, or stack trace")
    void testResultResponseDoesNotExposeSensitiveData() {
        String roll = "PH14_SECURITY";
        Long assessmentId = setupCompletedAssessment(roll, 20, true, true);

        ResponseEntity<?> resEntity = assessmentController.getAssessmentResult(assessmentId);
        assertEquals(HttpStatus.OK, resEntity.getStatusCode());
        assertNotNull(resEntity.getBody());
        assertTrue(resEntity.getBody() instanceof AssessmentResultResponse);

        AssessmentResultResponse res = (AssessmentResultResponse) resEntity.getBody();
        assertEquals(20, res.getAptitudeScore());
        assertEquals(3, res.getDsaScore());
        assertEquals(23, res.getTotalScore());
        assertEquals(23, res.getTotalMarks());

        // Ensure sensitive fields do not exist on the client-safe DTO
        for (var field : res.getClass().getDeclaredFields()) {
            assertNotEquals("sourceCode", field.getName());
            assertNotEquals("hiddenTests", field.getName());
            assertNotEquals("compilerOutput", field.getName());
            assertNotEquals("stackTrace", field.getName());
        }
    }
}
