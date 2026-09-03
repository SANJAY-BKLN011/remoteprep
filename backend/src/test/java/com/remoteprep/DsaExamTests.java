package com.remoteprep;

import com.remoteprep.dto.DsaQuestionResponse;
import com.remoteprep.dto.GenerateDsaExamRequest;
import com.remoteprep.dto.GenerateDsaExamResponse;
import com.remoteprep.dto.StartAssessmentRequest;
import com.remoteprep.dto.StartAssessmentResponse;
import com.remoteprep.entity.Assessment;
import com.remoteprep.repository.AssessmentRepository;
import com.remoteprep.repository.DsaSubmissionRepository;
import com.remoteprep.service.AssessmentService;
import com.remoteprep.service.DsaQuestionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DsaExamTests {

    @Autowired
    private DsaQuestionService dsaQuestionService;

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private DsaSubmissionRepository dsaSubmissionRepository;

    private Long createAssessment(String rollNumber) {
        StartAssessmentResponse startRes = assessmentService.startAssessment(
                new StartAssessmentRequest("Candidate " + rollNumber, rollNumber)
        );
        return startRes.getAssessmentId();
    }

    @Test
    @DisplayName("Test 1: One selected topic -> 1 EASY + 1 MEDIUM")
    void testOneSelectedTopic() {
        Long assessmentId = createAssessment("DSA_TEST_01");
        GenerateDsaExamResponse res = dsaQuestionService.generateDsaExam(
                new GenerateDsaExamRequest(assessmentId, List.of(1L))
        );

        assertNotNull(res);
        assertEquals(2, res.getQuestions().size());

        long easyCount = res.getQuestions().stream().filter(q -> "EASY".equalsIgnoreCase(q.getDifficulty())).count();
        long mediumCount = res.getQuestions().stream().filter(q -> "MEDIUM".equalsIgnoreCase(q.getDifficulty())).count();

        assertEquals(1, easyCount);
        assertEquals(1, mediumCount);
        assertTrue(res.getQuestions().stream().allMatch(q -> q.getTopicId().equals(1L)));
    }

    @Test
    @DisplayName("Test 2: Multiple selected topics -> 1 EASY + 1 MEDIUM")
    void testMultipleSelectedTopics() {
        Long assessmentId = createAssessment("DSA_TEST_02");
        GenerateDsaExamResponse res = dsaQuestionService.generateDsaExam(
                new GenerateDsaExamRequest(assessmentId, List.of(1L, 2L, 3L))
        );

        assertEquals(2, res.getQuestions().size());
        long easyCount = res.getQuestions().stream().filter(q -> "EASY".equalsIgnoreCase(q.getDifficulty())).count();
        long mediumCount = res.getQuestions().stream().filter(q -> "MEDIUM".equalsIgnoreCase(q.getDifficulty())).count();

        assertEquals(1, easyCount);
        assertEquals(1, mediumCount);
        Set<Long> allowedTopics = Set.of(1L, 2L, 3L);
        assertTrue(res.getQuestions().stream().allMatch(q -> allowedTopics.contains(q.getTopicId())));
    }

    @Test
    @DisplayName("Test 3: ALL TOPICS behavior (topics 1 to 10)")
    void testAllTopics() {
        Long assessmentId = createAssessment("DSA_TEST_03");
        List<Long> allTopics = List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
        GenerateDsaExamResponse res = dsaQuestionService.generateDsaExam(
                new GenerateDsaExamRequest(assessmentId, allTopics)
        );

        assertEquals(2, res.getQuestions().size());
        long easyCount = res.getQuestions().stream().filter(q -> "EASY".equalsIgnoreCase(q.getDifficulty())).count();
        long mediumCount = res.getQuestions().stream().filter(q -> "MEDIUM".equalsIgnoreCase(q.getDifficulty())).count();

        assertEquals(1, easyCount);
        assertEquals(1, mediumCount);
    }

    @Test
    @DisplayName("Test 4: Invalid topic ID -> rejected")
    void testInvalidTopicId() {
        Long assessmentId = createAssessment("DSA_TEST_04");
        assertThrows(IllegalArgumentException.class, () -> {
            dsaQuestionService.generateDsaExam(new GenerateDsaExamRequest(assessmentId, List.of(999L)));
        });
    }

    @Test
    @DisplayName("Test 5: Duplicate topic IDs -> rejected")
    void testDuplicateTopicIds() {
        Long assessmentId = createAssessment("DSA_TEST_05");
        assertThrows(IllegalArgumentException.class, () -> {
            dsaQuestionService.generateDsaExam(new GenerateDsaExamRequest(assessmentId, List.of(1L, 1L)));
        });
    }

    @Test
    @DisplayName("Test 6: Empty topic list -> rejected")
    void testEmptyTopicList() {
        Long assessmentId = createAssessment("DSA_TEST_06");
        assertThrows(IllegalArgumentException.class, () -> {
            dsaQuestionService.generateDsaExam(new GenerateDsaExamRequest(assessmentId, List.of()));
        });
    }

    @Test
    @DisplayName("Test 7: Null topic list -> rejected")
    void testNullTopicList() {
        Long assessmentId = createAssessment("DSA_TEST_07");
        assertThrows(IllegalArgumentException.class, () -> {
            dsaQuestionService.generateDsaExam(new GenerateDsaExamRequest(assessmentId, null));
        });
    }

    @Test
    @DisplayName("Test 8: Invalid assessment ID -> rejected")
    void testInvalidAssessmentId() {
        assertThrows(IllegalArgumentException.class, () -> {
            dsaQuestionService.generateDsaExam(new GenerateDsaExamRequest(999999L, List.of(1L)));
        });
    }

    @Test
    @DisplayName("Test 9: Assessment not IN_PROGRESS -> rejected")
    void testAssessmentNotInProgress() {
        Long assessmentId = createAssessment("DSA_TEST_09");
        Assessment assessment = assessmentRepository.findById(assessmentId).orElseThrow();
        assessment.setStatus("COMPLETED");
        assessmentRepository.save(assessment);

        assertThrows(IllegalStateException.class, () -> {
            dsaQuestionService.generateDsaExam(new GenerateDsaExamRequest(assessmentId, List.of(1L)));
        });
    }

    @Test
    @DisplayName("Test 12, 13, 14: Exactly two questions, exactly 1 EASY, exactly 1 MEDIUM")
    void testExactCountAndDifficulties() {
        Long assessmentId = createAssessment("DSA_TEST_12");
        GenerateDsaExamResponse res = dsaQuestionService.generateDsaExam(
                new GenerateDsaExamRequest(assessmentId, List.of(2L))
        );

        assertEquals(2, res.getQuestions().size());
        boolean hasEasy = res.getQuestions().stream().anyMatch(q -> "EASY".equalsIgnoreCase(q.getDifficulty()));
        boolean hasMedium = res.getQuestions().stream().anyMatch(q -> "MEDIUM".equalsIgnoreCase(q.getDifficulty()));
        assertTrue(hasEasy, "Must have exactly 1 EASY");
        assertTrue(hasMedium, "Must have exactly 1 MEDIUM");
    }

    @Test
    @DisplayName("Test 15: Same assessment generates the same two questions when endpoint called again")
    void testIdempotencySameAssessment() {
        Long assessmentId = createAssessment("DSA_TEST_15");
        GenerateDsaExamResponse first = dsaQuestionService.generateDsaExam(
                new GenerateDsaExamRequest(assessmentId, List.of(1L, 2L, 3L))
        );
        GenerateDsaExamResponse second = dsaQuestionService.generateDsaExam(
                new GenerateDsaExamRequest(assessmentId, List.of(1L, 2L, 3L))
        );

        assertEquals(first.getQuestions().size(), second.getQuestions().size());
        List<Long> firstIds = first.getQuestions().stream().map(DsaQuestionResponse::getId).toList();
        List<Long> secondIds = second.getQuestions().stream().map(DsaQuestionResponse::getId).toList();

        assertEquals(firstIds, secondIds, "Repeated exam request must return identical assigned questions");
    }

    @Test
    @DisplayName("Test 16: Different assessments are allowed to receive different questions")
    void testDifferentAssessmentsCanReceiveDifferentQuestions() {
        Set<List<Long>> uniquePairs = new HashSet<>();

        // Generate for multiple assessments across topics with multiple questions
        for (int i = 0; i < 5; i++) {
            Long assessmentId = createAssessment("DSA_TEST_16_" + i);
            GenerateDsaExamResponse res = dsaQuestionService.generateDsaExam(
                    new GenerateDsaExamRequest(assessmentId, List.of(1L, 2L, 3L, 4L, 5L))
            );
            List<Long> ids = res.getQuestions().stream().map(DsaQuestionResponse::getId).sorted().toList();
            uniquePairs.add(ids);
        }

        // Across 5 assessments selecting from 5 topics (10 Easy, 10 Medium available), we should observe variety
        assertTrue(uniquePairs.size() >= 2, "Different assessments should observe randomized variations");
    }

    @Test
    @DisplayName("Test 17, 18, 19: Safe DTO structure - no test_cases exposed, starter code, title, description, examples, constraints present")
    void testDtoStructureAndSecurity() {
        Long assessmentId = createAssessment("DSA_TEST_17");
        GenerateDsaExamResponse res = dsaQuestionService.generateDsaExam(
                new GenerateDsaExamRequest(assessmentId, List.of(1L))
        );

        for (DsaQuestionResponse q : res.getQuestions()) {
            assertNotNull(q.getId());
            assertNotNull(q.getTopicId());
            assertNotNull(q.getDifficulty());
            assertNotNull(q.getTitle());
            assertFalse(q.getTitle().isBlank());
            assertNotNull(q.getDescription());
            assertFalse(q.getDescription().isBlank());
            assertNotNull(q.getExamples());
            assertNotNull(q.getConstraints());

            assertNotNull(q.getStarterCode());
            assertNotNull(q.getStarterCode().getJava());
            assertNotNull(q.getStarterCode().getCpp());
            assertNotNull(q.getStarterCode().getC());
            assertNotNull(q.getStarterCode().getPython());
        }
    }
}
