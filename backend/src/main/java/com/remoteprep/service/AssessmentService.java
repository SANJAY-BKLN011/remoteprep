package com.remoteprep.service;

import com.remoteprep.dto.AssessmentResultResponse;
import com.remoteprep.dto.CompleteAssessmentResponse;
import com.remoteprep.dto.StartAssessmentRequest;
import com.remoteprep.dto.StartAssessmentResponse;
import com.remoteprep.entity.AptitudeAnswer;
import com.remoteprep.entity.Assessment;
import com.remoteprep.entity.DsaExamQuestion;
import com.remoteprep.entity.DsaQuestion;
import com.remoteprep.entity.DsaSubmission;
import com.remoteprep.entity.Student;
import com.remoteprep.repository.AptitudeAnswerRepository;
import com.remoteprep.repository.AssessmentRepository;
import com.remoteprep.repository.DsaExamQuestionRepository;
import com.remoteprep.repository.DsaSubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service Layer for Assessment operations.
 * Coordinates student lookup/creation, assessment session generation,
 * and final assessment completion/scoring.
 */
@Service
public class AssessmentService {

    private final StudentService studentService;
    private final AssessmentRepository assessmentRepository;
    private final AptitudeAnswerRepository aptitudeAnswerRepository;
    private final DsaExamQuestionRepository dsaExamQuestionRepository;
    private final DsaSubmissionRepository dsaSubmissionRepository;

    public AssessmentService(StudentService studentService,
                             AssessmentRepository assessmentRepository,
                             AptitudeAnswerRepository aptitudeAnswerRepository,
                             DsaExamQuestionRepository dsaExamQuestionRepository,
                             DsaSubmissionRepository dsaSubmissionRepository) {
        this.studentService = studentService;
        this.assessmentRepository = assessmentRepository;
        this.aptitudeAnswerRepository = aptitudeAnswerRepository;
        this.dsaExamQuestionRepository = dsaExamQuestionRepository;
        this.dsaSubmissionRepository = dsaSubmissionRepository;
    }

    /**
     * Starts a new assessment attempt:
     * 1. Finds existing student or creates a new one by roll number.
     * 2. Creates a brand new Assessment attempt in 'IN_PROGRESS' state with current timestamp.
     * 3. Returns the student and assessment details.
     */
    @Transactional
    public StartAssessmentResponse startAssessment(StartAssessmentRequest request) {
        Student student = studentService.findOrCreateStudent(request.getName(), request.getRollNumber());

        Assessment assessment = new Assessment();
        assessment.setStudent(student);
        assessment.setStartedAt(LocalDateTime.now());
        assessment.setStatus("IN_PROGRESS");
        assessment.setAptitudeScore(0);
        assessment.setDsaScore(0);
        assessment.setTotalScore(0);
        assessment.setCompletedAt(null);

        Assessment savedAssessment = assessmentRepository.save(assessment);

        return new StartAssessmentResponse(
                student.getId(),
                savedAssessment.getId(),
                student.getName(),
                student.getRollNumber(),
                savedAssessment.getStatus()
        );
    }

    /**
     * Completes an assessment attempt, calculates final aptitude and DSA scores,
     * updates assessment status to COMPLETED atomically, and returns the final result.
     *
     * Idempotent: Calling on an already-COMPLETED assessment returns the existing result
     * without modifying scores, completedAt, or creating duplicate records.
     */
    @Transactional
    public CompleteAssessmentResponse completeAssessment(Long assessmentId) {
        if (assessmentId == null) {
            throw new IllegalArgumentException("assessmentId must be provided");
        }

        // 1. Validate assessment exists
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with ID: " + assessmentId));

        // 2. Validate assessment has an associated student
        Student student = assessment.getStudent();
        if (student == null) {
            throw new IllegalStateException("Assessment belongs to no student");
        }

        // 3. Idempotency: If already COMPLETED, return existing completed result directly
        if ("COMPLETED".equalsIgnoreCase(assessment.getStatus())) {
            return new CompleteAssessmentResponse(
                    assessment.getId(),
                    student.getId(),
                    student.getName(),
                    student.getRollNumber(),
                    assessment.getAptitudeScore() != null ? assessment.getAptitudeScore() : 0,
                    20,
                    assessment.getDsaScore() != null ? assessment.getDsaScore() : 0,
                    3,
                    assessment.getTotalScore() != null ? assessment.getTotalScore() : 0,
                    23,
                    assessment.getStatus(),
                    assessment.getCompletedAt()
            );
        }

        // 4. Validate assessment is currently IN_PROGRESS
        if (!"IN_PROGRESS".equalsIgnoreCase(assessment.getStatus())) {
            throw new IllegalStateException("Assessment is not in IN_PROGRESS state (current status: " + assessment.getStatus() + ")");
        }

        // 5. Validate Aptitude exam has been generated
        List<AptitudeAnswer> assignedAnswers = aptitudeAnswerRepository.findByAssessment_Id(assessmentId);
        if (assignedAnswers == null || assignedAnswers.isEmpty()) {
            throw new IllegalStateException("Aptitude exam has not been generated for assessment ID " + assessmentId);
        }

        // 6. Validate Aptitude contains exactly 20 assigned questions
        if (assignedAnswers.size() != 20) {
            throw new IllegalStateException("Aptitude exam must contain exactly 20 assigned questions, found: " + assignedAnswers.size());
        }

        // 7. Validate Aptitude has been submitted
        if (assessment.getAptitudeScore() == null) {
            throw new IllegalStateException("Aptitude exam has not been submitted yet for assessment ID " + assessmentId);
        }

        // 8. Validate DSA exam has been generated
        List<DsaExamQuestion> dsaQuestions = dsaExamQuestionRepository.findByAssessment_IdOrderByQuestionOrderAsc(assessmentId);
        if (dsaQuestions == null || dsaQuestions.isEmpty()) {
            throw new IllegalStateException("DSA exam has not been generated for assessment ID " + assessmentId);
        }

        // 9. Validate DSA contains exactly 2 assigned questions
        if (dsaQuestions.size() != 2) {
            throw new IllegalStateException("DSA exam must contain exactly 2 assigned questions, found: " + dsaQuestions.size());
        }

        // 10. Validate each DSA question has at least one submission and calculate DSA score
        int dsaScore = 0;
        for (DsaExamQuestion deq : dsaQuestions) {
            DsaQuestion q = deq.getQuestion();
            List<DsaSubmission> submissions = dsaSubmissionRepository
                    .findByAssessment_IdAndQuestion_IdOrderBySubmittedAtDescIdDesc(assessmentId, q.getId());
            if (submissions == null || submissions.isEmpty()) {
                throw new IllegalStateException("DSA Question ID " + q.getId() + " has never been submitted");
            }

            // Latest submission is at index 0 (ordered by submittedAt DESC, id DESC)
            DsaSubmission latestSub = submissions.get(0);
            if ("ACCEPTED".equalsIgnoreCase(latestSub.getResultStatus())) {
                if ("EASY".equalsIgnoreCase(q.getDifficulty())) {
                    dsaScore += 1;
                } else if ("MEDIUM".equalsIgnoreCase(q.getDifficulty())) {
                    dsaScore += 2;
                }
            }
        }

        // 11. Calculate final scores
        int aptitudeScore = assessment.getAptitudeScore();
        int totalScore = aptitudeScore + dsaScore;
        LocalDateTime completedAt = LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);

        // 12. Atomically persist completed assessment state
        assessment.setAptitudeScore(aptitudeScore);
        assessment.setDsaScore(dsaScore);
        assessment.setTotalScore(totalScore);
        assessment.setStatus("COMPLETED");
        assessment.setCompletedAt(completedAt);

        assessmentRepository.save(assessment);

        // 13. Return client-safe final result
        return new CompleteAssessmentResponse(
                assessment.getId(),
                student.getId(),
                student.getName(),
                student.getRollNumber(),
                aptitudeScore,
                20,
                dsaScore,
                3,
                totalScore,
                23,
                assessment.getStatus(),
                completedAt
        );
    }

    /**
     * Retrieves the finalized result of a completed assessment.
     * Strictly read-only: does not calculate scores, mutate database records, or execute candidate code.
     *
     * @param assessmentId ID of the assessment to retrieve
     * @return client-safe final result DTO
     */
    @Transactional(readOnly = true)
    public AssessmentResultResponse getAssessmentResult(Long assessmentId) {
        if (assessmentId == null) {
            throw new IllegalArgumentException("assessmentId must be provided");
        }

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with ID: " + assessmentId));

        Student student = assessment.getStudent();
        if (student == null) {
            throw new IllegalStateException("Assessment belongs to no student");
        }

        if (!"COMPLETED".equalsIgnoreCase(assessment.getStatus())) {
            throw new IllegalStateException("Assessment is not in COMPLETED state (current status: " + assessment.getStatus() + ")");
        }

        return new AssessmentResultResponse(
                assessment.getId(),
                student.getId(),
                student.getName(),
                student.getRollNumber(),
                assessment.getAptitudeScore() != null ? assessment.getAptitudeScore() : 0,
                20,
                assessment.getDsaScore() != null ? assessment.getDsaScore() : 0,
                3,
                assessment.getTotalScore() != null ? assessment.getTotalScore() : 0,
                23,
                assessment.getStatus(),
                assessment.getStartedAt(),
                assessment.getCompletedAt()
        );
    }
}
