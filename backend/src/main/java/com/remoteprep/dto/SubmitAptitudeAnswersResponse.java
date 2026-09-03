package com.remoteprep.dto;

/**
 * Server response returned after evaluating and persisting candidate answers.
 * Securely communicates score totals without exposing individual correct answers or answer keys.
 */
public class SubmitAptitudeAnswersResponse {

    private Long assessmentId;
    private Integer aptitudeScore;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer wrongAnswers;
    private Integer skippedAnswers;
    private String status;

    public SubmitAptitudeAnswersResponse() {
    }

    public SubmitAptitudeAnswersResponse(Long assessmentId, Integer aptitudeScore, Integer totalQuestions,
                                        Integer correctAnswers, Integer wrongAnswers, Integer skippedAnswers,
                                        String status) {
        this.assessmentId = assessmentId;
        this.aptitudeScore = aptitudeScore;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.wrongAnswers = wrongAnswers;
        this.skippedAnswers = skippedAnswers;
        this.status = status;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }

    public Integer getAptitudeScore() {
        return aptitudeScore;
    }

    public void setAptitudeScore(Integer aptitudeScore) {
        this.aptitudeScore = aptitudeScore;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Integer getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(Integer correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public Integer getWrongAnswers() {
        return wrongAnswers;
    }

    public void setWrongAnswers(Integer wrongAnswers) {
        this.wrongAnswers = wrongAnswers;
    }

    public Integer getSkippedAnswers() {
        return skippedAnswers;
    }

    public void setSkippedAnswers(Integer skippedAnswers) {
        this.skippedAnswers = skippedAnswers;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
