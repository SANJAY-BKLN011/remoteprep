package com.remoteprep;

import com.remoteprep.dto.DsaQuestionResponse;
import com.remoteprep.dto.GenerateDsaExamRequest;
import com.remoteprep.dto.GenerateDsaExamResponse;
import com.remoteprep.dto.StartAssessmentRequest;
import com.remoteprep.dto.StartAssessmentResponse;
import com.remoteprep.entity.Assessment;
import com.remoteprep.entity.DsaExamQuestion;
import com.remoteprep.entity.Student;
import com.remoteprep.repository.AssessmentRepository;
import com.remoteprep.repository.DsaExamQuestionRepository;
import com.remoteprep.repository.DsaQuestionRepository;
import com.remoteprep.repository.DsaSubmissionRepository;
import com.remoteprep.repository.DsaTopicRepository;
import com.remoteprep.service.AssessmentService;
import com.remoteprep.service.DsaQuestionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
    private DsaExamQuestionRepository dsaExamQuestionRepository;

    @Autowired
    private DsaSubmissionRepository dsaSubmissionRepository;

    private Long createAssessment(String rollNumber) {
        StartAssessmentResponse startRes = assessmentService.startAssessment(
                new StartAssessmentRequest("Candidate " + rollNumber, rollNumber)
        );
        return startRes.getAssessmentId();
    }

    @Test
    @DisplayName("Test 1: One selected topic -> exactly 2 questions")
    void testOneSelectedTopic() {
        Long assessmentId = createAssessment("DSA_CORR_01");
        GenerateDsaExamResponse res = dsaQuestionService.generateDsaExam(
                new GenerateDsaExamRequest(assessmentId, List.of(1L))
        );

        assertNotNull(res);
        assertEquals(2, res.getQuestions().size());
    }

    @Test
    @DisplayName("Test 2 & 5: Multiple selected topics -> exactly 2 questions from selected topics")
    void testMultipleSelectedTopics() {
        Long assessmentId = createAssessment("DSA_CORR_02");
        GenerateDsaExamResponse res = dsaQuestionService.generateDsaExam(
                new GenerateDsaExamRequest(assessmentId, List.of(1L, 2L, 3L))
        );

        assertEquals(2, res.getQuestions().size());
        Set<Long> allowedTopics = Set.of(1L, 2L, 3L);
        assertTrue(res.getQuestions().stream().allMatch(q -> allowedTopics.contains(q.getTopicId())),
                "All assigned questions must belong to the selected topics");
    }

    @Test
    @DisplayName("Test 3 & 4: Exactly one EASY and exactly one MEDIUM")
    void testExactlyOneEasyAndOneMedium() {
        Long assessmentId = createAssessment("DSA_CORR_03");
        GenerateDsaExamResponse res = dsaQuestionService.generateDsaExam(
                new GenerateDsaExamRequest(assessmentId, List.of(2L))
        );

        assertEquals(2, res.getQuestions().size());
        long easyCount = res.getQuestions().stream().filter(q -> "EASY".equalsIgnoreCase(q.getDifficulty())).count();
        long mediumCount = res.getQuestions().stream().filter(q -> "MEDIUM".equalsIgnoreCase(q.getDifficulty())).count();

        assertEquals(1, easyCount, "Must have exactly 1 EASY problem");
        assertEquals(1, mediumCount, "Must have exactly 1 MEDIUM problem");
    }

    @Test
    @DisplayName("Test 6: Duplicate topic IDs rejected")
    void testDuplicateTopicIdsRejected() {
        Long assessmentId = createAssessment("DSA_CORR_06");
        assertThrows(IllegalArgumentException.class, () -> {
            dsaQuestionService.generateDsaExam(new GenerateDsaExamRequest(assessmentId, List.of(1L, 1L)));
        });
    }

    @Test
    @DisplayName("Test 7: Empty topic list rejected")
    void testEmptyTopicListRejected() {
        Long assessmentId = createAssessment("DSA_CORR_07");
        assertThrows(IllegalArgumentException.class, () -> {
            dsaQuestionService.generateDsaExam(new GenerateDsaExamRequest(assessmentId, List.of()));
        });
    }

    @Test
    @DisplayName("Test 8: Null topic list rejected")
    void testNullTopicListRejected() {
        Long assessmentId = createAssessment("DSA_CORR_08");
        assertThrows(IllegalArgumentException.class, () -> {
            dsaQuestionService.generateDsaExam(new GenerateDsaExamRequest(assessmentId, null));
        });
    }

    @Test
    @DisplayName("Test 9: Invalid topic ID rejected")
    void testInvalidTopicIdRejected() {
        Long assessmentId = createAssessment("DSA_CORR_09");
        assertThrows(IllegalArgumentException.class, () -> {
            dsaQuestionService.generateDsaExam(new GenerateDsaExamRequest(assessmentId, List.of(99999L)));
        });
    }

    @Test
    @DisplayName("Test 10: Invalid assessment ID rejected")
    void testInvalidAssessmentIdRejected() {
        assertThrows(IllegalArgumentException.class, () -> {
            dsaQuestionService.generateDsaExam(new GenerateDsaExamRequest(999999L, List.of(1L)));
        });
    }

    @Test
    @DisplayName("Test 11: Assessment not IN_PROGRESS rejected")
    void testAssessmentNotInProgressRejected() {
        Long assessmentId = createAssessment("DSA_CORR_11");
        Assessment assessment = assessmentRepository.findById(assessmentId).orElseThrow();
        assessment.setStatus("COMPLETED");
        assessmentRepository.save(assessment);

        assertThrows(IllegalStateException.class, () -> {
            dsaQuestionService.generateDsaExam(new GenerateDsaExamRequest(assessmentId, List.of(1L)));
        });
    }

    @Test
    @DisplayName("Test 12: Assessment without student rejected")
    void testAssessmentWithoutStudentRejected() {
        Assessment mockAssessment = new Assessment();
        mockAssessment.setStatus("IN_PROGRESS");
        mockAssessment.setStudent(null);

        AssessmentRepository mockRepo = Mockito.mock(AssessmentRepository.class);
        Mockito.when(mockRepo.findById(999L)).thenReturn(Optional.of(mockAssessment));

        DsaQuestionService testService = new DsaQuestionService(
                null, null, mockRepo, null
        );

        assertThrows(IllegalStateException.class, () -> {
            testService.generateDsaExam(new GenerateDsaExamRequest(999L, List.of(1L)));
        });
    }

    @Test
    @DisplayName("Test 13: No EASY question available -> error")
    void testNoEasyAvailable() {
        Assessment mockAssessment = new Assessment();
        mockAssessment.setStatus("IN_PROGRESS");
        mockAssessment.setStudent(new Student("Test", "ROLL1"));

        AssessmentRepository mockAssRepo = Mockito.mock(AssessmentRepository.class);
        Mockito.when(mockAssRepo.findById(1L)).thenReturn(Optional.of(mockAssessment));

        DsaExamQuestionRepository mockExamQRepo = Mockito.mock(DsaExamQuestionRepository.class);
        Mockito.when(mockExamQRepo.findByAssessment_IdOrderByQuestionOrderAsc(1L)).thenReturn(List.of());

        DsaTopicRepository mockTopicRepo = Mockito.mock(DsaTopicRepository.class);
        Mockito.when(mockTopicRepo.existsById(1L)).thenReturn(true);

        DsaQuestionRepository mockQRepo = Mockito.mock(DsaQuestionRepository.class);
        Mockito.when(mockQRepo.countByTopic_IdInAndDifficulty(List.of(1L), "EASY")).thenReturn(0L);

        DsaQuestionService testService = new DsaQuestionService(
                mockQRepo, mockTopicRepo, mockAssRepo, mockExamQRepo
        );

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            testService.generateDsaExam(new GenerateDsaExamRequest(1L, List.of(1L)));
        });
        assertTrue(ex.getMessage().contains("No EASY question available"));
    }

    @Test
    @DisplayName("Test 14: No MEDIUM question available -> error")
    void testNoMediumAvailable() {
        Assessment mockAssessment = new Assessment();
        mockAssessment.setStatus("IN_PROGRESS");
        mockAssessment.setStudent(new Student("Test", "ROLL1"));

        AssessmentRepository mockAssRepo = Mockito.mock(AssessmentRepository.class);
        Mockito.when(mockAssRepo.findById(1L)).thenReturn(Optional.of(mockAssessment));

        DsaExamQuestionRepository mockExamQRepo = Mockito.mock(DsaExamQuestionRepository.class);
        Mockito.when(mockExamQRepo.findByAssessment_IdOrderByQuestionOrderAsc(1L)).thenReturn(List.of());

        DsaTopicRepository mockTopicRepo = Mockito.mock(DsaTopicRepository.class);
        Mockito.when(mockTopicRepo.existsById(1L)).thenReturn(true);

        DsaQuestionRepository mockQRepo = Mockito.mock(DsaQuestionRepository.class);
        Mockito.when(mockQRepo.countByTopic_IdInAndDifficulty(List.of(1L), "EASY")).thenReturn(1L);
        Mockito.when(mockQRepo.countByTopic_IdInAndDifficulty(List.of(1L), "MEDIUM")).thenReturn(0L);

        DsaQuestionService testService = new DsaQuestionService(
                mockQRepo, mockTopicRepo, mockAssRepo, mockExamQRepo
        );

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            testService.generateDsaExam(new GenerateDsaExamRequest(1L, List.of(1L)));
        });
        assertTrue(ex.getMessage().contains("No MEDIUM question available"));
    }

    @Test
    @DisplayName("Test 15: Repeated request for same assessment returns same two question IDs")
    void testIdempotencyRepeatedRequest() {
        Long assessmentId = createAssessment("DSA_CORR_15");
        GenerateDsaExamResponse first = dsaQuestionService.generateDsaExam(
                new GenerateDsaExamRequest(assessmentId, List.of(1L, 2L, 3L))
        );
        GenerateDsaExamResponse second = dsaQuestionService.generateDsaExam(
                new GenerateDsaExamRequest(assessmentId, List.of(1L, 2L, 3L))
        );

        List<Long> firstIds = first.getQuestions().stream().map(DsaQuestionResponse::getId).toList();
        List<Long> secondIds = second.getQuestions().stream().map(DsaQuestionResponse::getId).toList();

        assertEquals(firstIds, secondIds, "Repeated exam request must return identical assigned questions");
    }

    @Test
    @DisplayName("Test 16: Different assessments may receive different random questions")
    void testDifferentAssessmentsCanReceiveDifferentQuestions() {
        Set<List<Long>> uniqueSets = new HashSet<>();

        for (int i = 0; i < 5; i++) {
            Long assessmentId = createAssessment("DSA_CORR_16_" + i);
            GenerateDsaExamResponse res = dsaQuestionService.generateDsaExam(
                    new GenerateDsaExamRequest(assessmentId, List.of(1L, 2L, 3L, 4L, 5L))
            );
            List<Long> ids = res.getQuestions().stream().map(DsaQuestionResponse::getId).sorted().toList();
            uniqueSets.add(ids);
        }

        assertTrue(uniqueSets.size() >= 2, "Different assessments should observe randomized variations across questions");
    }

    @Test
    @DisplayName("Test 17: Same question must not be assigned twice to the same assessment")
    void testSameQuestionNotAssignedTwice() {
        Long assessmentId = createAssessment("DSA_CORR_17");
        GenerateDsaExamResponse res = dsaQuestionService.generateDsaExam(
                new GenerateDsaExamRequest(assessmentId, List.of(1L))
        );

        Long q1Id = res.getQuestions().get(0).getId();
        Long q2Id = res.getQuestions().get(1).getId();
        assertNotEquals(q1Id, q2Id, "The two assigned questions must have distinct IDs");
    }

    @Test
    @DisplayName("Test 18, 19, 20: Safe DTO structure - no test_cases exposed, starter code, title, description, examples, constraints present")
    void testDtoStructureAndSecurity() {
        Long assessmentId = createAssessment("DSA_CORR_18");
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

    @Test
    @DisplayName("Test 21, 22, 23: Verify dsa_submissions is NOT touched, exactly 2 rows in dsa_exam_questions, no rows added on repeat")
    void testCleanPersistenceSeparation() {
        Long assessmentId = createAssessment("DSA_CORR_PERSIST");

        long initialSubmissionsCount = dsaSubmissionRepository.count();
        long initialExamQuestionsCount = dsaExamQuestionRepository.countByAssessment_Id(assessmentId);
        assertEquals(0, initialExamQuestionsCount);

        // 1. Generate DSA exam
        GenerateDsaExamResponse res1 = dsaQuestionService.generateDsaExam(
                new GenerateDsaExamRequest(assessmentId, List.of(1L, 2L))
        );
        assertNotNull(res1);

        // Test 21: Verify POST /api/dsa/exam does NOT create rows in dsa_submissions
        long postSubmissionsCount = dsaSubmissionRepository.count();
        assertEquals(initialSubmissionsCount, postSubmissionsCount,
                "dsa_submissions must NOT receive any rows during DSA exam generation!");

        // Test 22: Verify exactly two rows are created in dsa_exam_questions for a new assessment
        List<DsaExamQuestion> assignedQuestions = dsaExamQuestionRepository.findByAssessment_IdOrderByQuestionOrderAsc(assessmentId);
        assertEquals(2, assignedQuestions.size(), "Exactly 2 rows must exist in dsa_exam_questions");
        assertEquals(1, assignedQuestions.get(0).getQuestionOrder());
        assertEquals(2, assignedQuestions.get(1).getQuestionOrder());

        // Test 23: Verify repeated generation does not create additional dsa_exam_questions rows
        dsaQuestionService.generateDsaExam(new GenerateDsaExamRequest(assessmentId, List.of(1L, 2L)));
        long repeatedExamQuestionsCount = dsaExamQuestionRepository.countByAssessment_Id(assessmentId);
        assertEquals(2, repeatedExamQuestionsCount, "Repeated exam generation must not create extra rows in dsa_exam_questions");

        long finalSubmissionsCount = dsaSubmissionRepository.count();
        assertEquals(initialSubmissionsCount, finalSubmissionsCount, "dsa_submissions count must remain unchanged");
    }
}
