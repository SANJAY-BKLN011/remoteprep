package com.remoteprep.dto;

import java.util.List;

/**
 * Top-level response returned by POST /api/dsa/run.
 * Contains results for exactly two demo test cases.
 * Strictly excludes internal JPA entities and hidden test cases.
 */
public class DsaRunResponse {

    private Long assessmentId;
    private Long questionId;
    private String language;
    private List<DsaRunTestCaseResult> testCases;

    public DsaRunResponse() {
    }

    public DsaRunResponse(Long assessmentId, Long questionId, String language, List<DsaRunTestCaseResult> testCases) {
        this.assessmentId = assessmentId;
        this.questionId = questionId;
        this.language = language;
        this.testCases = testCases;
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

    public List<DsaRunTestCaseResult> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<DsaRunTestCaseResult> testCases) {
        this.testCases = testCases;
    }
}
