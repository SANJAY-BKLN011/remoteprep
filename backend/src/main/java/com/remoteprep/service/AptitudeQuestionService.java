package com.remoteprep.service;

import com.remoteprep.dto.AptitudeQuestionResponse;
import com.remoteprep.dto.GenerateExamRequest;
import com.remoteprep.dto.GenerateExamResponse;
import com.remoteprep.dto.SubmitAptitudeAnswersRequest;
import com.remoteprep.dto.SubmitAptitudeAnswersResponse;
import com.remoteprep.dto.SubmittedAnswerItem;
import com.remoteprep.entity.AptitudeAnswer;
import com.remoteprep.entity.AptitudeQuestion;
import com.remoteprep.entity.AptitudeTopic;
import com.remoteprep.entity.Assessment;
import com.remoteprep.repository.AptitudeAnswerRepository;
import com.remoteprep.repository.AptitudeQuestionRepository;
import com.remoteprep.repository.AptitudeTopicRepository;
import com.remoteprep.repository.AssessmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service Layer for Aptitude Exam Question Retrieval, Random Generation, and Answer Evaluation.
 */
@Service
public class AptitudeQuestionService {

    private final AptitudeQuestionRepository aptitudeQuestionRepository;
    private final AptitudeTopicRepository aptitudeTopicRepository;
    private final AssessmentRepository assessmentRepository;
    private final AptitudeAnswerRepository aptitudeAnswerRepository;

    public AptitudeQuestionService(AptitudeQuestionRepository aptitudeQuestionRepository,
                                  AptitudeTopicRepository aptitudeTopicRepository,
                                  AssessmentRepository assessmentRepository,
                                  AptitudeAnswerRepository aptitudeAnswerRepository) {
        this.aptitudeQuestionRepository = aptitudeQuestionRepository;
        this.aptitudeTopicRepository = aptitudeTopicRepository;
        this.assessmentRepository = assessmentRepository;
        this.aptitudeAnswerRepository = aptitudeAnswerRepository;
    }

    /**
     * Generates or retrieves a 20-question aptitude exam for an active assessment attempt.
     * Persists assigned questions to 'aptitude_answers' to guarantee question assignment immutability.
     */
    @Transactional
    public GenerateExamResponse generateExam(GenerateExamRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }

        Long assessmentId = request.getAssessmentId();
        if (assessmentId == null) {
            throw new IllegalArgumentException("Assessment ID must be provided");
        }

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with ID: " + assessmentId));

        if (!"IN_PROGRESS".equalsIgnoreCase(assessment.getStatus())) {
            throw new IllegalStateException("Assessment is not in IN_PROGRESS state (current status: " + assessment.getStatus() + ")");
        }

        if (assessment.getStudent() == null) {
            throw new IllegalStateException("Assessment is not associated with any student");
        }

        // Check if exam questions have ALREADY been assigned to this assessment
        List<AptitudeAnswer> existingAnswers = aptitudeAnswerRepository.findByAssessment_Id(assessmentId);
        if (existingAnswers.size() == 20) {
            // Return existing questions without re-randomizing or creating duplicates
            List<AptitudeQuestionResponse> existingQuestions = new ArrayList<>(20);
            for (AptitudeAnswer ans : existingAnswers) {
                AptitudeQuestion q = ans.getQuestion();
                existingQuestions.add(new AptitudeQuestionResponse(
                        q.getId(),
                        q.getTopicId(),
                        q.getQuestionText(),
                        q.getOptionA(),
                        q.getOptionB(),
                        q.getOptionC(),
                        q.getOptionD()
                ));
            }
            return new GenerateExamResponse(assessmentId, existingQuestions);
        }

        List<Long> topicIds = request.getTopicIds();
        if (topicIds == null || topicIds.isEmpty()) {
            throw new IllegalArgumentException("At least 1 aptitude topic must be selected");
        }

        if (topicIds.size() > 3) {
            throw new IllegalArgumentException("Maximum of 3 aptitude topics can be selected, but received " + topicIds.size());
        }

        Set<Long> uniqueTopics = new HashSet<>(topicIds);
        if (uniqueTopics.size() != topicIds.size()) {
            throw new IllegalArgumentException("Duplicate topic IDs are not allowed");
        }

        // Validate each topic ID exists in aptitude_topics
        for (Long topicId : topicIds) {
            if (topicId == null || !aptitudeTopicRepository.existsById(topicId)) {
                throw new IllegalArgumentException("Invalid topic ID: " + topicId);
            }
        }

        // Calculate distribution:
        // 1 topic -> 20
        // 2 topics -> 10, 10
        // 3 topics -> 7, 7, 6
        int[] distribution;
        if (topicIds.size() == 1) {
            distribution = new int[]{20};
        } else if (topicIds.size() == 2) {
            distribution = new int[]{10, 10};
        } else {
            distribution = new int[]{7, 7, 6};
        }

        // Verify question availability for each selected topic
        for (int i = 0; i < topicIds.size(); i++) {
            Long topicId = topicIds.get(i);
            int required = distribution[i];
            long available = aptitudeQuestionRepository.countByTopic_Id(topicId);

            if (available < required) {
                AptitudeTopic topic = aptitudeTopicRepository.findById(topicId).orElse(null);
                String topicName = topic != null ? topic.getTopicName() : String.valueOf(topicId);
                throw new IllegalStateException("Insufficient questions for topic '" + topicName +
                        "' (ID " + topicId + "). Required: " + required + ", Available: " + available);
            }
        }

        // Retrieve randomized questions per topic
        List<AptitudeQuestion> allSelectedQuestions = new ArrayList<>(20);
        Set<Long> uniqueQuestionIds = new HashSet<>(20);

        for (int i = 0; i < topicIds.size(); i++) {
            Long topicId = topicIds.get(i);
            int required = distribution[i];

            List<AptitudeQuestion> topicQuestions = aptitudeQuestionRepository.findRandomQuestionsByTopicId(topicId, required);

            for (AptitudeQuestion q : topicQuestions) {
                if (uniqueQuestionIds.add(q.getId())) {
                    allSelectedQuestions.add(q);
                }
            }
        }

        if (allSelectedQuestions.size() != 20) {
            throw new IllegalStateException("Expected exactly 20 questions, but selected " + allSelectedQuestions.size());
        }

        // Randomize overall question presentation order
        Collections.shuffle(allSelectedQuestions);

        // Persist initial 20 assigned question records in 'aptitude_answers'
        List<AptitudeAnswer> assignedAnswers = new ArrayList<>(20);
        for (AptitudeQuestion q : allSelectedQuestions) {
            assignedAnswers.add(new AptitudeAnswer(assessment, q));
        }
        aptitudeAnswerRepository.saveAll(assignedAnswers);

        // Reset aptitude score to null to clearly signify unsubmitted exam state
        assessment.setAptitudeScore(null);
        assessmentRepository.save(assessment);

        // Map to client-safe DTOs (DO NOT expose correct answer or explanation)
        List<AptitudeQuestionResponse> questionDtos = new ArrayList<>(20);
        for (AptitudeQuestion q : allSelectedQuestions) {
            questionDtos.add(new AptitudeQuestionResponse(
                    q.getId(),
                    q.getTopicId(),
                    q.getQuestionText(),
                    q.getOptionA(),
                    q.getOptionB(),
                    q.getOptionC(),
                    q.getOptionD()
            ));
        }

        return new GenerateExamResponse(assessmentId, questionDtos);
    }

    /**
     * Evaluates and records candidate answers for an aptitude examination.
     * All scoring is performed server-side by comparing against 'aptitude_questions.correct_option'.
     */
    @Transactional
    public SubmitAptitudeAnswersResponse submitAnswers(SubmitAptitudeAnswersRequest request) {
        // 1. Validate request body and assessmentId
        if (request == null) {
            throw new IllegalArgumentException("Request body must exist");
        }

        Long assessmentId = request.getAssessmentId();
        if (assessmentId == null) {
            throw new IllegalArgumentException("assessmentId must exist");
        }

        // 2. Validate assessment exists and status is IN_PROGRESS
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with ID: " + assessmentId));

        if (!"IN_PROGRESS".equalsIgnoreCase(assessment.getStatus())) {
            throw new IllegalStateException("Assessment status must be IN_PROGRESS (current status: " + assessment.getStatus() + ")");
        }

        // 3. Validate assigned questions exist in aptitude_answers
        List<AptitudeAnswer> assignedAnswers = aptitudeAnswerRepository.findByAssessment_Id(assessmentId);
        if (assignedAnswers == null || assignedAnswers.isEmpty()) {
            throw new IllegalStateException("The assessment must have an aptitude exam generated before submitting answers");
        }

        if (assignedAnswers.size() != 20) {
            throw new IllegalStateException("The assessment must contain exactly 20 assigned aptitude questions, found: " + assignedAnswers.size());
        }

        // 4. Duplicate submission check: Assessment must only be submitted once
        if (assessment.getAptitudeScore() != null || assignedAnswers.stream().anyMatch(a -> a.getAnsweredAt() != null)) {
            throw new IllegalStateException("Aptitude answers have already been submitted for this assessment");
        }

        // 5. Validate submitted answer list
        List<SubmittedAnswerItem> submittedList = request.getAnswers();
        if (submittedList == null) {
            submittedList = Collections.emptyList();
        }

        if (submittedList.size() > 20) {
            throw new IllegalArgumentException("No more than 20 answers may be submitted, received: " + submittedList.size());
        }

        // Map assigned questions by ID for quick lookup
        Map<Long, AptitudeAnswer> assignedMap = new HashMap<>();
        for (AptitudeAnswer ans : assignedAnswers) {
            assignedMap.put(ans.getQuestion().getId(), ans);
        }

        // Validate submitted question IDs and check for duplicates
        Set<Long> seenQuestionIds = new HashSet<>();
        Map<Long, String> clientOptionsByQId = new HashMap<>();

        for (SubmittedAnswerItem item : submittedList) {
            if (item == null || item.getQuestionId() == null) {
                throw new IllegalArgumentException("Submitted answer must have a non-null questionId");
            }

            Long qId = item.getQuestionId();

            if (!seenQuestionIds.add(qId)) {
                throw new IllegalArgumentException("Duplicate question ID in request: " + qId);
            }

            if (!assignedMap.containsKey(qId)) {
                throw new IllegalArgumentException("Question ID " + qId + " was not assigned to this assessment");
            }

            String opt = item.getSelectedOption();
            if (opt != null) {
                String trimmed = opt.trim();
                if (trimmed.isEmpty()) {
                    opt = null;
                } else {
                    String upper = trimmed.toUpperCase();
                    if (!upper.equals("A") && !upper.equals("B") && !upper.equals("C") && !upper.equals("D")) {
                        throw new IllegalArgumentException("Invalid selected option: '" + opt + "'. Option must be A, B, C, D, or null");
                    }
                    opt = upper;
                }
            }
            clientOptionsByQId.put(qId, opt);
        }

        // 6. Server-side scoring
        int correctCount = 0;
        int wrongCount = 0;
        int skippedCount = 0;
        LocalDateTime now = LocalDateTime.now();

        for (AptitudeAnswer answerRecord : assignedAnswers) {
            AptitudeQuestion question = answerRecord.getQuestion();
            Long qId = question.getId();
            String submittedOption = clientOptionsByQId.get(qId);

            if (submittedOption == null) {
                // Skipped / unanswered question
                answerRecord.setSelectedOption(null);
                answerRecord.setIsCorrect(false);
                answerRecord.setAnsweredAt(null);
                skippedCount++;
            } else {
                // Answered question
                answerRecord.setSelectedOption(submittedOption);
                answerRecord.setAnsweredAt(now);

                String correctOption = question.getCorrectOption();
                boolean isMatch = correctOption != null && correctOption.equalsIgnoreCase(submittedOption);

                answerRecord.setIsCorrect(isMatch);
                if (isMatch) {
                    correctCount++;
                } else {
                    wrongCount++;
                }
            }
        }

        // Save evaluated answers
        aptitudeAnswerRepository.saveAll(assignedAnswers);

        // 7. Update assessment scores while maintaining IN_PROGRESS status
        int dsaScore = assessment.getDsaScore() != null ? assessment.getDsaScore() : 0;
        assessment.setAptitudeScore(correctCount);
        assessment.setTotalScore(correctCount + dsaScore);
        // Status remains IN_PROGRESS pending subsequent DSA phase
        assessmentRepository.save(assessment);

        return new SubmitAptitudeAnswersResponse(
                assessment.getId(),
                correctCount,
                20,
                correctCount,
                wrongCount,
                skippedCount,
                assessment.getStatus()
        );
    }
}
