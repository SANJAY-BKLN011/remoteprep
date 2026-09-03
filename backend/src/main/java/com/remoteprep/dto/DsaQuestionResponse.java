package com.remoteprep.dto;

/**
 * Client-facing DSA Question DTO.
 * Excludes internal test_cases and execution keys for academic integrity.
 */
public class DsaQuestionResponse {

    private Long id;
    private Long topicId;
    private String difficulty;
    private String title;
    private String description;
    private String examples;
    private String constraints;
    private StarterCodeDto starterCode;

    public DsaQuestionResponse() {
    }

    public DsaQuestionResponse(Long id, Long topicId, String difficulty, String title,
                              String description, String examples, String constraints,
                              StarterCodeDto starterCode) {
        this.id = id;
        this.topicId = topicId;
        this.difficulty = difficulty;
        this.title = title;
        this.description = description;
        this.examples = examples;
        this.constraints = constraints;
        this.starterCode = starterCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
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

    public StarterCodeDto getStarterCode() {
        return starterCode;
    }

    public void setStarterCode(StarterCodeDto starterCode) {
        this.starterCode = starterCode;
    }
}
