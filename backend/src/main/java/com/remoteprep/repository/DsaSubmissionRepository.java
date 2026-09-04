package com.remoteprep.repository;

import com.remoteprep.entity.DsaSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for DsaSubmission entity.
 * Reserved exclusively for candidate actual source code submissions.
 */
@Repository
public interface DsaSubmissionRepository extends JpaRepository<DsaSubmission, Long> {

    /**
     * Retrieves all code submissions for an assessment ordered chronologically descending.
     */
    List<DsaSubmission> findByAssessment_IdOrderBySubmittedAtDesc(Long assessmentId);

    /**
     * Retrieves all code submissions for a specific question within an assessment, ordered descending.
     */
    List<DsaSubmission> findByAssessment_IdAndQuestion_IdOrderBySubmittedAtDesc(Long assessmentId, Long questionId);

    /**
     * Retrieves all code submissions for a specific question within an assessment,
     * ordered strictly by submittedAt descending with secondary deterministic tie-breaker id descending.
     */
    List<DsaSubmission> findByAssessment_IdAndQuestion_IdOrderBySubmittedAtDescIdDesc(Long assessmentId, Long questionId);

    /**
     * Counts actual code submissions for a specific question within an assessment.
     */
    long countByAssessment_IdAndQuestion_Id(Long assessmentId, Long questionId);

    /**
     * Counts all code submissions for an assessment.
     */
    long countByAssessment_Id(Long assessmentId);
}
