package com.remoteprep.dto;

/**
 * Response payload returned when an assessment attempt is initiated.
 */
public class StartAssessmentResponse {

    private Long studentId;
    private Long assessmentId;
    private String name;
    private String rollNumber;
    private String status;

    public StartAssessmentResponse() {
    }

    public StartAssessmentResponse(Long studentId, Long assessmentId, String name, String rollNumber, String status) {
        this.studentId = studentId;
        this.assessmentId = assessmentId;
        this.name = name;
        this.rollNumber = rollNumber;
        this.status = status;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
