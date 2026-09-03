package com.remoteprep.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

/**
 * JPA Entity mapping to the 'dsa_submissions' table in MySQL.
 * Tracks candidate assigned DSA questions (initially with result_status='UNATTEMPTED')
 * and records subsequent code submissions and verification verdicts.
 */
@Entity
@Table(name = "dsa_submissions")
public class DsaSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private DsaQuestion question;

    @Column(name = "language", nullable = false, length = 20)
    private String language;

    @Column(name = "source_code", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String sourceCode;

    @Column(name = "result_status", nullable = false)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String resultStatus;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    public DsaSubmission() {
    }

    public DsaSubmission(Assessment assessment, DsaQuestion question, String language, String sourceCode, String resultStatus, LocalDateTime submittedAt) {
        this.assessment = assessment;
        this.question = question;
        this.language = language;
        this.sourceCode = sourceCode;
        this.resultStatus = resultStatus;
        this.submittedAt = submittedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }

    public DsaQuestion getQuestion() {
        return question;
    }

    public void setQuestion(DsaQuestion question) {
        this.question = question;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
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
