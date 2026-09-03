package com.remoteprep.dto;

/**
 * Request payload for starting an assessment.
 */
public class StartAssessmentRequest {

    private String name;
    private String rollNumber;

    public StartAssessmentRequest() {
    }

    public StartAssessmentRequest(String name, String rollNumber) {
        this.name = name;
        this.rollNumber = rollNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }
}
