package com.remoteprep.service;

import com.remoteprep.dto.StartAssessmentRequest;
import com.remoteprep.dto.StartAssessmentResponse;
import com.remoteprep.entity.Assessment;
import com.remoteprep.entity.Student;
import com.remoteprep.repository.AssessmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service Layer for Assessment operations.
 * Coordinates student lookup/creation and assessment session generation.
 */
@Service
public class AssessmentService {

    private final StudentService studentService;
    private final AssessmentRepository assessmentRepository;

    public AssessmentService(StudentService studentService, AssessmentRepository assessmentRepository) {
        this.studentService = studentService;
        this.assessmentRepository = assessmentRepository;
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
}
