package com.remoteprep.repository;

import com.remoteprep.entity.DsaSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for DsaSubmission entity.
 */
@Repository
public interface DsaSubmissionRepository extends JpaRepository<DsaSubmission, Long> {

    /**
     * Retrieves assigned DSA questions/submissions for an assessment.
     */
    List<DsaSubmission> findByAssessment_Id(Long assessmentId);

    /**
     * Counts assigned DSA submissions for an assessment.
     */
    long countByAssessment_Id(Long assessmentId);
}
