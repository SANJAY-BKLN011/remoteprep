package com.remoteprep.repository;

import com.remoteprep.entity.AptitudeQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for AptitudeQuestion entity.
 */
@Repository
public interface AptitudeQuestionRepository extends JpaRepository<AptitudeQuestion, Long> {

    /**
     * Counts questions available for a specific topic ID.
     */
    long countByTopic_Id(Long topicId);

    /**
     * Selects N random questions for a specific topic ID.
     * Uses MySQL's native RAND() with LIMIT for lightweight execution.
     */
    @Query(value = "SELECT * FROM aptitude_questions WHERE topic_id = :topicId ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<AptitudeQuestion> findRandomQuestionsByTopicId(@Param("topicId") Long topicId, @Param("limit") int limit);
}
