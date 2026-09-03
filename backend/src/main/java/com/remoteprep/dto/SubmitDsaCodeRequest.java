package com.remoteprep.dto;

/**
 * Request payload for submitting DSA source code.
 */
public class SubmitDsaCodeRequest {

    private Long assessmentId;
    private Long questionId;
    private String language;
    private String sourceCode;

    public SubmitDsaCodeRequest() {
    }

    public SubmitDsaCodeRequest(Long assessmentId, Long questionId, String language, String sourceCode) {
        this.assessmentId = assessmentId;
        this.questionId = questionId;
        this.language = language;
        this.sourceCode = sourceCode;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }
}
