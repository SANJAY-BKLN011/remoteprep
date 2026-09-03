package com.remoteprep.dto;

/**
 * Client-facing question response DTO.
 * Excludes correctOption, explanation, and answer keys for examination integrity.
 */
public class AptitudeQuestionResponse {

    private Long id;
    private Long topicId;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    public AptitudeQuestionResponse() {
    }

    public AptitudeQuestionResponse(Long id, Long topicId, String questionText,
                                   String optionA, String optionB, String optionC, String optionD) {
        this.id = id;
        this.topicId = topicId;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
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

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }
}
