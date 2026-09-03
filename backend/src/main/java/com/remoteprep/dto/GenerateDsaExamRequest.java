package com.remoteprep.dto;

import java.util.List;

/**
 * Request payload for generating a DSA examination.
 */
public class GenerateDsaExamRequest {

    private Long assessmentId;
    private List<Long> topicIds;

    public GenerateDsaExamRequest() {
    }

    public GenerateDsaExamRequest(Long assessmentId, List<Long> topicIds) {
        this.assessmentId = assessmentId;
        this.topicIds = topicIds;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }

    public List<Long> getTopicIds() {
        return topicIds;
    }

    public void setTopicIds(List<Long> topicIds) {
        this.topicIds = topicIds;
    }
}
