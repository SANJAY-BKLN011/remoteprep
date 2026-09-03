package com.remoteprep.dto;

import java.util.List;

/**
 * Response payload containing the randomized 20-question exam.
 */
public class GenerateExamResponse {

    private Long assessmentId;
    private List<AptitudeQuestionResponse> questions;

    public GenerateExamResponse() {
    }

    public GenerateExamResponse(Long assessmentId, List<AptitudeQuestionResponse> questions) {
        this.assessmentId = assessmentId;
        this.questions = questions;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }

    public List<AptitudeQuestionResponse> getQuestions() {
        return questions;
    }

    public void setQuestions(List<AptitudeQuestionResponse> questions) {
        this.questions = questions;
    }
}
