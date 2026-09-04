package com.remoteprep.dto;

import java.time.LocalDateTime;

/**
 * Response payload returned after persisting a candidate's DSA code submission.
 * Strictly excludes internal test cases, hidden expected outputs, and execution details.
 */
public class SubmitDsaCodeResponse {

    private Long submissionId;
    private Long assessmentId;
    private Long questionId;
    private String language;
    private String resultStatus;
    private String status;
    private LocalDateTime submittedAt;
    private Integer totalTestCases;
    private Integer passedTestCases;
    private Integer failedTestCases;
    private Long executionTimeMs;

    public SubmitDsaCodeResponse() {
    }

    public SubmitDsaCodeResponse(Long submissionId, Long assessmentId, Long questionId,
                                 String language, String resultStatus, LocalDateTime submittedAt) {
        this.submissionId = submissionId;
        this.assessmentId = assessmentId;
        this.questionId = questionId;
        this.language = language;
        this.resultStatus = resultStatus;
        this.status = resultStatus;
        this.submittedAt = submittedAt;
    }

    public SubmitDsaCodeResponse(Long submissionId, Long assessmentId, Long questionId,
                                 String language, String status, LocalDateTime submittedAt,
                                 Integer totalTestCases, Integer passedTestCases,
                                 Integer failedTestCases, Long executionTimeMs) {
        this.submissionId = submissionId;
        this.assessmentId = assessmentId;
        this.questionId = questionId;
        this.language = language;
        this.resultStatus = status;
        this.status = status;
        this.submittedAt = submittedAt;
        this.totalTestCases = totalTestCases;
        this.passedTestCases = passedTestCases;
        this.failedTestCases = failedTestCases;
        this.executionTimeMs = executionTimeMs;
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
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

    public String getResultStatus() {
        return resultStatus != null ? resultStatus : status;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
        this.status = resultStatus;
    }

    public String getStatus() {
        return status != null ? status : resultStatus;
    }

    public void setStatus(String status) {
        this.status = status;
        this.resultStatus = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Integer getTotalTestCases() {
        return totalTestCases;
    }

    public void setTotalTestCases(Integer totalTestCases) {
        this.totalTestCases = totalTestCases;
    }

    public Integer getPassedTestCases() {
        return passedTestCases;
    }

    public void setPassedTestCases(Integer passedTestCases) {
        this.passedTestCases = passedTestCases;
    }

    public Integer getFailedTestCases() {
        return failedTestCases;
    }

    public void setFailedTestCases(Integer failedTestCases) {
        this.failedTestCases = failedTestCases;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
}
