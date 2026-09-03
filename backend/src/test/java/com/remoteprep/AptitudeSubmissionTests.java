package com.remoteprep;

import com.remoteprep.dto.GenerateExamRequest;
import com.remoteprep.dto.GenerateExamResponse;
import com.remoteprep.dto.StartAssessmentRequest;
import com.remoteprep.dto.StartAssessmentResponse;
import com.remoteprep.dto.SubmitAptitudeAnswersRequest;
import com.remoteprep.dto.SubmitAptitudeAnswersResponse;
import com.remoteprep.dto.SubmittedAnswerItem;
import com.remoteprep.entity.AptitudeAnswer;
import com.remoteprep.entity.AptitudeQuestion;
import com.remoteprep.entity.Assessment;
import com.remoteprep.repository.AptitudeAnswerRepository;
import com.remoteprep.repository.AssessmentRepository;
import com.remoteprep.service.AptitudeQuestionService;
import com.remoteprep.service.AssessmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AptitudeSubmissionTests {

    @Autowired
    private AptitudeQuestionService aptitudeQuestionService;

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private AptitudeAnswerRepository aptitudeAnswerRepository;

    private Long createAssessmentWithExam(String rollNumber) {
        StartAssessmentResponse startRes = assessmentService.startAssessment(
                new StartAssessmentRequest("Candidate " + rollNumber, rollNumber)
        );
        Long assessmentId = startRes.getAssessmentId();
        aptitudeQuestionService.generateExam(new GenerateExamRequest(assessmentId, List.of(1L)));
        return assessmentId;
    }

    @Test
    @DisplayName("Test 1: 20 correct answers -> score 20")
    void testTwentyCorrectAnswers() {
        Long assessmentId = createAssessmentWithExam("TEST_ROLL_P7_01");
        List<AptitudeAnswer> assigned = aptitudeAnswerRepository.findByAssessment_Id(assessmentId);
        assertEquals(20, assigned.size());

        List<SubmittedAnswerItem> answers = new ArrayList<>();
        for (AptitudeAnswer ans : assigned) {
            String correct = ans.getQuestion().getCorrectOption();
            answers.add(new SubmittedAnswerItem(ans.getQuestion().getId(), correct));
        }

        SubmitAptitudeAnswersResponse res = aptitudeQuestionService.submitAnswers(
                new SubmitAptitudeAnswersRequest(assessmentId, answers)
        );

        assertEquals(20, res.getAptitudeScore());
        assertEquals(20, res.getCorrectAnswers());
        assertEquals(0, res.getWrongAnswers());
        assertEquals(0, res.getSkippedAnswers());
        assertEquals(20, res.getTotalQuestions());
        assertEquals("IN_PROGRESS", res.getStatus());
    }

    @Test
    @DisplayName("Test 2: All wrong answers -> score 0")
    void testAllWrongAnswers() {
        Long assessmentId = createAssessmentWithExam("TEST_ROLL_P7_02");
        List<AptitudeAnswer> assigned = aptitudeAnswerRepository.findByAssessment_Id(assessmentId);

        List<SubmittedAnswerItem> answers = new ArrayList<>();
        for (AptitudeAnswer ans : assigned) {
            String correct = ans.getQuestion().getCorrectOption();
            // Choose an intentionally wrong option
            String wrong = "A".equalsIgnoreCase(correct) ? "B" : "A";
            answers.add(new SubmittedAnswerItem(ans.getQuestion().getId(), wrong));
        }

        SubmitAptitudeAnswersResponse res = aptitudeQuestionService.submitAnswers(
                new SubmitAptitudeAnswersRequest(assessmentId, answers)
        );

        assertEquals(0, res.getAptitudeScore());
        assertEquals(0, res.getCorrectAnswers());
        assertEquals(20, res.getWrongAnswers());
        assertEquals(0, res.getSkippedAnswers());
    }

    @Test
    @DisplayName("Test 3: Mix of correct/wrong/skipped -> score accurately reflects correct answers")
    void testMixOfAnswers() {
        Long assessmentId = createAssessmentWithExam("TEST_ROLL_P7_03");
        List<AptitudeAnswer> assigned = aptitudeAnswerRepository.findByAssessment_Id(assessmentId);

        // 10 correct, 5 wrong, 5 skipped
        List<SubmittedAnswerItem> answers = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            AptitudeAnswer ans = assigned.get(i);
            String correct = ans.getQuestion().getCorrectOption();
            if (i < 10) {
                // Correct
                answers.add(new SubmittedAnswerItem(ans.getQuestion().getId(), correct));
            } else if (i < 15) {
                // Wrong
                String wrong = "A".equalsIgnoreCase(correct) ? "B" : "A";
                answers.add(new SubmittedAnswerItem(ans.getQuestion().getId(), wrong));
            } else {
                // Skipped (null)
                answers.add(new SubmittedAnswerItem(ans.getQuestion().getId(), null));
            }
        }

        SubmitAptitudeAnswersResponse res = aptitudeQuestionService.submitAnswers(
                new SubmitAptitudeAnswersRequest(assessmentId, answers)
        );

        assertEquals(10, res.getAptitudeScore());
        assertEquals(10, res.getCorrectAnswers());
        assertEquals(5, res.getWrongAnswers());
        assertEquals(5, res.getSkippedAnswers());
    }

    @Test
    @DisplayName("Test 4: All skipped -> score 0")
    void testAllSkipped() {
        Long assessmentId = createAssessmentWithExam("TEST_ROLL_P7_04");
        List<AptitudeAnswer> assigned = aptitudeAnswerRepository.findByAssessment_Id(assessmentId);

        List<SubmittedAnswerItem> answers = new ArrayList<>();
        for (AptitudeAnswer ans : assigned) {
            answers.add(new SubmittedAnswerItem(ans.getQuestion().getId(), null));
        }

        SubmitAptitudeAnswersResponse res = aptitudeQuestionService.submitAnswers(
                new SubmitAptitudeAnswersRequest(assessmentId, answers)
        );

        assertEquals(0, res.getAptitudeScore());
        assertEquals(0, res.getCorrectAnswers());
        assertEquals(0, res.getWrongAnswers());
        assertEquals(20, res.getSkippedAnswers());
    }

    @Test
    @DisplayName("Test 5: Invalid assessment ID -> rejected")
    void testInvalidAssessmentId() {
        assertThrows(IllegalArgumentException.class, () -> {
            aptitudeQuestionService.submitAnswers(
                    new SubmitAptitudeAnswersRequest(9999999L, List.of())
            );
        });
    }

    @Test
    @DisplayName("Test 6: Assessment not IN_PROGRESS -> rejected")
    void testAssessmentNotInProgress() {
        Long assessmentId = createAssessmentWithExam("TEST_ROLL_P7_06");
        Assessment assessment = assessmentRepository.findById(assessmentId).orElseThrow();
        assessment.setStatus("COMPLETED");
        assessmentRepository.save(assessment);

        assertThrows(IllegalStateException.class, () -> {
            aptitudeQuestionService.submitAnswers(
                    new SubmitAptitudeAnswersRequest(assessmentId, List.of())
            );
        });
    }

    @Test
    @DisplayName("Test 7: Question ID not belonging to assessment -> rejected")
    void testQuestionNotBelongingToAssessment() {
        Long assessmentId = createAssessmentWithExam("TEST_ROLL_P7_07");

        List<SubmittedAnswerItem> answers = List.of(
                new SubmittedAnswerItem(999999L, "A")
        );

        assertThrows(IllegalArgumentException.class, () -> {
            aptitudeQuestionService.submitAnswers(
                    new SubmitAptitudeAnswersRequest(assessmentId, answers)
            );
        });
    }

    @Test
    @DisplayName("Test 8: Duplicate question IDs -> rejected")
    void testDuplicateQuestionIds() {
        Long assessmentId = createAssessmentWithExam("TEST_ROLL_P7_08");
        List<AptitudeAnswer> assigned = aptitudeAnswerRepository.findByAssessment_Id(assessmentId);
        Long qId = assigned.get(0).getQuestion().getId();

        List<SubmittedAnswerItem> answers = List.of(
                new SubmittedAnswerItem(qId, "A"),
                new SubmittedAnswerItem(qId, "B")
        );

        assertThrows(IllegalArgumentException.class, () -> {
            aptitudeQuestionService.submitAnswers(
                    new SubmitAptitudeAnswersRequest(assessmentId, answers)
            );
        });
    }

    @Test
    @DisplayName("Test 9: Invalid selected option such as 'X' -> rejected")
    void testInvalidSelectedOption() {
        Long assessmentId = createAssessmentWithExam("TEST_ROLL_P7_09");
        List<AptitudeAnswer> assigned = aptitudeAnswerRepository.findByAssessment_Id(assessmentId);
        Long qId = assigned.get(0).getQuestion().getId();

        List<SubmittedAnswerItem> answers = List.of(
                new SubmittedAnswerItem(qId, "X")
        );

        assertThrows(IllegalArgumentException.class, () -> {
            aptitudeQuestionService.submitAnswers(
                    new SubmitAptitudeAnswersRequest(assessmentId, answers)
            );
        });
    }

    @Test
    @DisplayName("Test 10: More than 20 submitted answers -> rejected")
    void testMoreThanTwentyAnswers() {
        Long assessmentId = createAssessmentWithExam("TEST_ROLL_P7_10");
        List<SubmittedAnswerItem> answers = new ArrayList<>();
        for (long i = 1; i <= 25; i++) {
            answers.add(new SubmittedAnswerItem(i, "A"));
        }

        assertThrows(IllegalArgumentException.class, () -> {
            aptitudeQuestionService.submitAnswers(
                    new SubmitAptitudeAnswersRequest(assessmentId, answers)
            );
        });
    }

    @Test
    @DisplayName("Test 11: Duplicate submission must fail")
    void testDuplicateSubmissionFails() {
        Long assessmentId = createAssessmentWithExam("TEST_ROLL_P7_11");
        List<AptitudeAnswer> assigned = aptitudeAnswerRepository.findByAssessment_Id(assessmentId);

        List<SubmittedAnswerItem> answers = List.of(
                new SubmittedAnswerItem(assigned.get(0).getQuestion().getId(), "A")
        );

        // First submission succeeds
        SubmitAptitudeAnswersResponse first = aptitudeQuestionService.submitAnswers(
                new SubmitAptitudeAnswersRequest(assessmentId, answers)
        );
        assertNotNull(first);

        // Second submission attempt must fail
        assertThrows(IllegalStateException.class, () -> {
            aptitudeQuestionService.submitAnswers(
                    new SubmitAptitudeAnswersRequest(assessmentId, answers)
            );
        });
    }

    @Test
    @DisplayName("Test 12 & 13: Client cannot manipulate score or supply isCorrect")
    void testClientCannotManipulateScore() {
        Long assessmentId = createAssessmentWithExam("TEST_ROLL_P7_12");
        List<AptitudeAnswer> assigned = aptitudeAnswerRepository.findByAssessment_Id(assessmentId);

        // All wrong answers submitted
        List<SubmittedAnswerItem> answers = new ArrayList<>();
        for (AptitudeAnswer ans : assigned) {
            String correct = ans.getQuestion().getCorrectOption();
            String wrong = "A".equalsIgnoreCase(correct) ? "B" : "A";
            answers.add(new SubmittedAnswerItem(ans.getQuestion().getId(), wrong));
        }

        SubmitAptitudeAnswersResponse res = aptitudeQuestionService.submitAnswers(
                new SubmitAptitudeAnswersRequest(assessmentId, answers)
        );

        // Score must strictly be 0 based on server comparison
        assertEquals(0, res.getAptitudeScore());
    }

    @Test
    @DisplayName("Test 14: total_score = aptitude_score + dsa_score")
    void testTotalScoreComputation() {
        Long assessmentId = createAssessmentWithExam("TEST_ROLL_P7_14");
        Assessment assessment = assessmentRepository.findById(assessmentId).orElseThrow();
        assessment.setDsaScore(50); // Simulate pre-existing DSA score
        assessmentRepository.save(assessment);

        List<AptitudeAnswer> assigned = aptitudeAnswerRepository.findByAssessment_Id(assessmentId);
        List<SubmittedAnswerItem> answers = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            AptitudeAnswer ans = assigned.get(i);
            answers.add(new SubmittedAnswerItem(ans.getQuestion().getId(), ans.getQuestion().getCorrectOption()));
        }

        SubmitAptitudeAnswersResponse res = aptitudeQuestionService.submitAnswers(
                new SubmitAptitudeAnswersRequest(assessmentId, answers)
        );

        assertEquals(5, res.getAptitudeScore());

        Assessment updated = assessmentRepository.findById(assessmentId).orElseThrow();
        assertEquals(5, updated.getAptitudeScore());
        assertEquals(50, updated.getDsaScore());
        assertEquals(55, updated.getTotalScore()); // 5 + 50
    }

    @Test
    @DisplayName("Test 15: Assessment remains IN_PROGRESS after aptitude submission")
    void testAssessmentRemainsInProgress() {
        Long assessmentId = createAssessmentWithExam("TEST_ROLL_P7_15");
        SubmitAptitudeAnswersResponse res = aptitudeQuestionService.submitAnswers(
                new SubmitAptitudeAnswersRequest(assessmentId, List.of())
        );

        assertEquals("IN_PROGRESS", res.getStatus());
        Assessment updated = assessmentRepository.findById(assessmentId).orElseThrow();
        assertEquals("IN_PROGRESS", updated.getStatus());
    }
}
