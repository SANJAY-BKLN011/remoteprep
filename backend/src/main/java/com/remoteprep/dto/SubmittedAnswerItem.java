package com.remoteprep.dto;

/**
 * Individual question response submitted by the candidate.
 * Server receives only questionId and selectedOption (A, B, C, D, or null).
 */
public class SubmittedAnswerItem {

    private Long questionId;
    private String selectedOption;

    public SubmittedAnswerItem() {
    }

    public SubmittedAnswerItem(Long questionId, String selectedOption) {
        this.questionId = questionId;
        this.selectedOption = selectedOption;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(String selectedOption) {
        this.selectedOption = selectedOption;
    }
}
