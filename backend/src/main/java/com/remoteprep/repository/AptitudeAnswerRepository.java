package com.remoteprep.repository;

import com.remoteprep.entity.AptitudeAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for AptitudeAnswer entity.
 */
@Repository
public interface AptitudeAnswerRepository extends JpaRepository<AptitudeAnswer, Long> {

    /**
     * Retrieves all assigned aptitude answers/questions for a specific assessment.
     */
    List<AptitudeAnswer> findByAssessment_Id(Long assessmentId);

    /**
     * Counts assigned aptitude questions for an assessment.
     */
    long countByAssessment_Id(Long assessmentId);
}
