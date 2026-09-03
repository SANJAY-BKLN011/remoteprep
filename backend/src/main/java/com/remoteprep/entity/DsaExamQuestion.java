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
import java.time.LocalDateTime;

/**
 * JPA Entity mapping to the 'dsa_exam_questions' table in MySQL.
 * Represents the assigned DSA questions for an assessment session (1 EASY, 1 MEDIUM).
 */
@Entity
@Table(name = "dsa_exam_questions")
public class DsaExamQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private DsaQuestion question;

    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;

    @Column(name = "assigned_at", insertable = false, updatable = false)
    private LocalDateTime assignedAt;

    public DsaExamQuestion() {
    }

    public DsaExamQuestion(Assessment assessment, DsaQuestion question, Integer questionOrder) {
        this.assessment = assessment;
        this.question = question;
        this.questionOrder = questionOrder;
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

    public Integer getQuestionOrder() {
        return questionOrder;
    }

    public void setQuestionOrder(Integer questionOrder) {
        this.questionOrder = questionOrder;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}
