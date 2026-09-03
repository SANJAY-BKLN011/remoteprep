package com.remoteprep.repository;

import com.remoteprep.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository for Assessment entity.
 */
@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
}
