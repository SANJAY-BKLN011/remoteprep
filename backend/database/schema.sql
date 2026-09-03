-- =============================================================================
-- RemotePrep Assessment Platform - MySQL Database Schema
-- Database: remoteprep
-- Engine: InnoDB | Character Set: utf8mb4 | Collation: utf8mb4_unicode_ci
-- =============================================================================

-- 1. Create Database if not exists
CREATE DATABASE IF NOT EXISTS remoteprep
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE remoteprep;

-- Disable foreign key checks during schema re-creation if executed cleanly
SET FOREIGN_KEY_CHECKS = 0;

-- Drop tables in reverse dependency order
DROP TABLE IF EXISTS dsa_submissions;
DROP TABLE IF EXISTS aptitude_answers;
DROP TABLE IF EXISTS assessments;
DROP TABLE IF EXISTS dsa_questions;
DROP TABLE IF EXISTS dsa_topics;
DROP TABLE IF EXISTS aptitude_questions;
DROP TABLE IF EXISTS aptitude_topics;
DROP TABLE IF EXISTS students;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- TABLE 1: students
-- Stores candidate identity and demographic metadata.
-- =============================================================================
CREATE TABLE students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    roll_number VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_student_roll (roll_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- TABLE 2: aptitude_topics
-- Stores the 32 normalized Aptitude syllabus topics across 4 main categories.
-- =============================================================================
CREATE TABLE aptitude_topics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    topic_code VARCHAR(50) NOT NULL UNIQUE,
    category VARCHAR(50) NOT NULL,
    topic_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_apt_topic_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- TABLE 3: aptitude_questions
-- Stores normalized MCQ questions linked to an aptitude topic.
-- =============================================================================
CREATE TABLE aptitude_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    topic_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    option_a TEXT NOT NULL,
    option_b TEXT NOT NULL,
    option_c TEXT NOT NULL,
    option_d TEXT NOT NULL,
    correct_option CHAR(1) NOT NULL COMMENT 'A, B, C, or D',
    explanation TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_apt_q_topic FOREIGN KEY (topic_id) 
        REFERENCES aptitude_topics(id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_apt_q_topic_id (topic_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- TABLE 4: dsa_topics
-- Stores the 10 normalized DSA syllabus topics.
-- =============================================================================
CREATE TABLE dsa_topics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    topic_code VARCHAR(50) NOT NULL UNIQUE,
    topic_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- TABLE 5: dsa_questions
-- Stores DSA coding problems with 4-language starter templates and test cases.
-- =============================================================================
CREATE TABLE dsa_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    topic_id BIGINT NOT NULL,
    difficulty ENUM('EASY', 'MEDIUM', 'HARD') NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    examples TEXT NULL,
    constraints TEXT NULL,
    starter_java MEDIUMTEXT NULL,
    starter_cpp MEDIUMTEXT NULL,
    starter_c MEDIUMTEXT NULL,
    starter_python MEDIUMTEXT NULL,
    test_cases MEDIUMTEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dsa_q_topic FOREIGN KEY (topic_id) 
        REFERENCES dsa_topics(id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_dsa_q_topic_diff (topic_id, difficulty)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- TABLE 6: assessments
-- Tracks a candidate's complete assessment session (Aptitude + DSA).
-- =============================================================================
CREATE TABLE assessments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    started_at DATETIME NOT NULL,
    completed_at DATETIME NULL,
    aptitude_score INT DEFAULT 0,
    dsa_score INT DEFAULT 0,
    total_score INT DEFAULT 0,
    status ENUM('IN_PROGRESS', 'COMPLETED', 'ABANDONED') DEFAULT 'IN_PROGRESS',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_assessment_student FOREIGN KEY (student_id) 
        REFERENCES students(id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_assessment_student (student_id),
    INDEX idx_assessment_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- TABLE 7: aptitude_answers
-- Records the candidate's selected response for each aptitude question in a test.
-- =============================================================================
CREATE TABLE aptitude_answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assessment_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    selected_option CHAR(1) NULL COMMENT 'A, B, C, D, or NULL if skipped',
    is_correct BOOLEAN DEFAULT FALSE,
    answered_at DATETIME NULL,
    CONSTRAINT fk_ans_assessment FOREIGN KEY (assessment_id) 
        REFERENCES assessments(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_ans_question FOREIGN KEY (question_id) 
        REFERENCES aptitude_questions(id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_ans_assessment_q (assessment_id, question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- TABLE 8: dsa_exam_questions
-- Stores the assigned DSA questions (1 EASY, 1 MEDIUM) for an assessment.
-- =============================================================================
CREATE TABLE dsa_exam_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assessment_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    question_order INT NOT NULL COMMENT '1 for EASY, 2 for MEDIUM',
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dsa_eq_assessment FOREIGN KEY (assessment_id) 
        REFERENCES assessments(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_dsa_eq_question FOREIGN KEY (question_id) 
        REFERENCES dsa_questions(id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    UNIQUE KEY uq_dsa_eq_assessment_question (assessment_id, question_id),
    UNIQUE KEY uq_dsa_eq_assessment_order (assessment_id, question_order),
    INDEX idx_dsa_eq_assessment (assessment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- TABLE 9: dsa_submissions
-- Records the candidate's submitted source code and verification verdict for DSA.
-- =============================================================================
CREATE TABLE dsa_submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assessment_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    language VARCHAR(20) NOT NULL COMMENT 'java, cpp, c, python',
    source_code MEDIUMTEXT NOT NULL,
    result_status ENUM('ACCEPTED', 'WRONG_ANSWER', 'COMPILATION_ERROR', 'RUNTIME_ERROR', 'TIME_LIMIT_EXCEEDED', 'UNATTEMPTED', 'PENDING') NOT NULL,
    submitted_at DATETIME NOT NULL,
    CONSTRAINT fk_sub_assessment FOREIGN KEY (assessment_id) 
        REFERENCES assessments(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_sub_question FOREIGN KEY (question_id) 
        REFERENCES dsa_questions(id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_sub_assessment_q (assessment_id, question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
