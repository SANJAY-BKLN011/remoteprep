package com.remoteprep.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * JPA Entity mapping to the 'aptitude_topics' table in MySQL.
 */
@Entity
@Table(name = "aptitude_topics")
public class AptitudeTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic_code", nullable = false, unique = true, length = 50)
    private String topicCode;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "topic_name", nullable = false, length = 100)
    private String topicName;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // Default no-args constructor (required by JPA / Hibernate)
    public AptitudeTopic() {
    }

    // Parameterized constructor
    public AptitudeTopic(String topicCode, String category, String topicName) {
        this.topicCode = topicCode;
        this.category = category;
        this.topicName = topicName;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTopicCode() {
        return topicCode;
    }

    public void setTopicCode(String topicCode) {
        this.topicCode = topicCode;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
