package com.remoteprep.dto;

import java.util.List;

/**
 * Request payload for submitting candidate answers for an aptitude assessment.
 */
public class SubmitAptitudeAnswersRequest {

    private Long assessmentId;
    private List<SubmittedAnswerItem> answers;

    public SubmitAptitudeAnswersRequest() {
    }

    public SubmitAptitudeAnswersRequest(Long assessmentId, List<SubmittedAnswerItem> answers) {
        this.assessmentId = assessmentId;
        this.answers = answers;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }

    public List<SubmittedAnswerItem> getAnswers() {
        return answers;
    }

    public void setAnswers(List<SubmittedAnswerItem> answers) {
        this.answers = answers;
    }
}
