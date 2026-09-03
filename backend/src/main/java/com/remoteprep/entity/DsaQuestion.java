package com.remoteprep.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

/**
 * JPA Entity mapping to the 'dsa_questions' table in MySQL.
 */
@Entity
@Table(name = "dsa_questions")
public class DsaQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private DsaTopic topic;

    @Column(name = "difficulty", nullable = false)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String difficulty;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "examples", columnDefinition = "TEXT")
    private String examples;

    @Column(name = "constraints", columnDefinition = "TEXT")
    private String constraints;

    @Column(name = "starter_java", columnDefinition = "MEDIUMTEXT")
    private String starterJava;

    @Column(name = "starter_cpp", columnDefinition = "MEDIUMTEXT")
    private String starterCpp;

    @Column(name = "starter_c", columnDefinition = "MEDIUMTEXT")
    private String starterC;

    @Column(name = "starter_python", columnDefinition = "MEDIUMTEXT")
    private String starterPython;

    @Column(name = "test_cases", columnDefinition = "MEDIUMTEXT")
    private String testCases;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public DsaQuestion() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DsaTopic getTopic() {
        return topic;
    }

    public void setTopic(DsaTopic topic) {
        this.topic = topic;
    }

    public Long getTopicId() {
        return topic != null ? topic.getId() : null;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExamples() {
        return examples;
    }

    public void setExamples(String examples) {
        this.examples = examples;
    }

    public String getConstraints() {
        return constraints;
    }

    public void setConstraints(String constraints) {
        this.constraints = constraints;
    }

    public String getStarterJava() {
        return starterJava;
    }

    public void setStarterJava(String starterJava) {
        this.starterJava = starterJava;
    }

    public String getStarterCpp() {
        return starterCpp;
    }

    public void setStarterCpp(String starterCpp) {
        this.starterCpp = starterCpp;
    }

    public String getStarterC() {
        return starterC;
    }

    public void setStarterC(String starterC) {
        this.starterC = starterC;
    }

    public String getStarterPython() {
        return starterPython;
    }

    public void setStarterPython(String starterPython) {
        this.starterPython = starterPython;
    }

    public String getTestCases() {
        return testCases;
    }

    public void setTestCases(String testCases) {
        this.testCases = testCases;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
