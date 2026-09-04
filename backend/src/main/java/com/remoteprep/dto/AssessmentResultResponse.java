package com.remoteprep.dto;

import java.time.LocalDateTime;

/**
 * Client-safe response DTO for retrieving the finalized result of an assessment.
 * Strictly read-only; excludes JPA entity graphs, hidden test cases, source code, and compiler internals.
 */
public class AssessmentResultResponse {

    private Long assessmentId;
    private Long studentId;
    private String studentName;
    private String rollNumber;

    private Integer aptitudeScore;
    private Integer aptitudeTotal;

    private Integer dsaScore;
    private Integer dsaTotal;

    private Integer totalScore;
    private Integer totalMarks;

    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public AssessmentResultResponse() {
    }

    public AssessmentResultResponse(Long assessmentId,
                                    Long studentId,
                                    String studentName,
                                    String rollNumber,
                                    Integer aptitudeScore,
                                    Integer aptitudeTotal,
                                    Integer dsaScore,
                                    Integer dsaTotal,
                                    Integer totalScore,
                                    Integer totalMarks,
                                    String status,
                                    LocalDateTime startedAt,
                                    LocalDateTime completedAt) {
        this.assessmentId = assessmentId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.rollNumber = rollNumber;
        this.aptitudeScore = aptitudeScore;
        this.aptitudeTotal = aptitudeTotal;
        this.dsaScore = dsaScore;
        this.dsaTotal = dsaTotal;
        this.totalScore = totalScore;
        this.totalMarks = totalMarks;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public Integer getAptitudeScore() {
        return aptitudeScore;
    }

    public void setAptitudeScore(Integer aptitudeScore) {
        this.aptitudeScore = aptitudeScore;
    }

    public Integer getAptitudeTotal() {
        return aptitudeTotal;
    }

    public void setAptitudeTotal(Integer aptitudeTotal) {
        this.aptitudeTotal = aptitudeTotal;
    }

    public Integer getDsaScore() {
        return dsaScore;
    }

    public void setDsaScore(Integer dsaScore) {
        this.dsaScore = dsaScore;
    }

    public Integer getDsaTotal() {
        return dsaTotal;
    }

    public void setDsaTotal(Integer dsaTotal) {
        this.dsaTotal = dsaTotal;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public Integer getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Integer totalMarks) {
        this.totalMarks = totalMarks;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
