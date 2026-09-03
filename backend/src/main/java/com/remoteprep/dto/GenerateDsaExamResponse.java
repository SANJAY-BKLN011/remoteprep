package com.remoteprep.dto;

import java.util.List;

/**
 * Response payload containing the assigned DSA problems (1 EASY + 1 MEDIUM).
 */
public class GenerateDsaExamResponse {

    private Long assessmentId;
    private List<DsaQuestionResponse> questions;

    public GenerateDsaExamResponse() {
    }

    public GenerateDsaExamResponse(Long assessmentId, List<DsaQuestionResponse> questions) {
        this.assessmentId = assessmentId;
        this.questions = questions;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }

    public List<DsaQuestionResponse> getQuestions() {
        return questions;
    }

    public void setQuestions(List<DsaQuestionResponse> questions) {
        this.questions = questions;
    }
}
