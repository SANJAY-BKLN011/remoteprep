package com.remoteprep.repository;

import com.remoteprep.entity.DsaTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository for DsaTopic entity.
 */
@Repository
public interface DsaTopicRepository extends JpaRepository<DsaTopic, Long> {
}
