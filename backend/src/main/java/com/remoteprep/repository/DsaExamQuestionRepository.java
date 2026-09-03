package com.remoteprep.repository;

import com.remoteprep.entity.DsaExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for DsaExamQuestion entity.
 * Source of truth for assigned DSA questions per assessment.
 */
@Repository
public interface DsaExamQuestionRepository extends JpaRepository<DsaExamQuestion, Long> {

    /**
     * Retrieves assigned DSA questions for an assessment ordered by question_order.
     */
    List<DsaExamQuestion> findByAssessment_IdOrderByQuestionOrderAsc(Long assessmentId);

    /**
     * Counts assigned DSA questions for an assessment.
     */
    long countByAssessment_Id(Long assessmentId);
}
