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
    private LocalDateTime submittedAt;

    public SubmitDsaCodeResponse() {
    }

    public SubmitDsaCodeResponse(Long submissionId, Long assessmentId, Long questionId,
                                 String language, String resultStatus, LocalDateTime submittedAt) {
        this.submissionId = submissionId;
        this.assessmentId = assessmentId;
        this.questionId = questionId;
        this.language = language;
        this.resultStatus = resultStatus;
        this.submittedAt = submittedAt;
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
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}
