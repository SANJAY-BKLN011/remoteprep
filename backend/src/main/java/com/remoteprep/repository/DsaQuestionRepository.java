package com.remoteprep.repository;

import com.remoteprep.entity.DsaQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for DsaQuestion entity.
 */
@Repository
public interface DsaQuestionRepository extends JpaRepository<DsaQuestion, Long> {

    /**
     * Counts available questions for a specific list of topic IDs and difficulty.
     */
    long countByTopic_IdInAndDifficulty(List<Long> topicIds, String difficulty);

    /**
     * Selects one random question from the selected topics matching the given difficulty.
     * Uses MySQL native RAND() with LIMIT 1.
     */
    @Query(value = "SELECT * FROM dsa_questions WHERE topic_id IN (:topicIds) AND difficulty = :difficulty ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<DsaQuestion> findRandomQuestionByTopicIdsAndDifficulty(@Param("topicIds") List<Long> topicIds, @Param("difficulty") String difficulty);
}
