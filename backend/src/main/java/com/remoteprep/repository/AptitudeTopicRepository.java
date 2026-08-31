package com.remoteprep.repository;

import com.remoteprep.entity.AptitudeTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository for AptitudeTopic entity.
 * Provides standard CRUD and pagination operations automatically.
 */
@Repository
public interface AptitudeTopicRepository extends JpaRepository<AptitudeTopic, Long> {
}
