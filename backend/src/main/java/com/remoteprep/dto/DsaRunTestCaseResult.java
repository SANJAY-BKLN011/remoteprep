package com.remoteprep.dto;

/**
 * Result model for an individual demo test case execution.
 * Encapsulates input, expected output, actual output, status, error, and runtime.
 */
public class DsaRunTestCaseResult {

    private Integer testCaseNumber;
    private String input;
    private String expectedOutput;
    private String actualOutput;
    private String status;
    private String error;
    private Long executionTimeMs;

    public DsaRunTestCaseResult() {
    }

    public DsaRunTestCaseResult(Integer testCaseNumber, String input, String expectedOutput,
                                String actualOutput, String status, String error, Long executionTimeMs) {
        this.testCaseNumber = testCaseNumber;
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.actualOutput = actualOutput;
        this.status = status;
        this.error = error;
        this.executionTimeMs = executionTimeMs;
    }

    public Integer getTestCaseNumber() {
        return testCaseNumber;
    }

    public void setTestCaseNumber(Integer testCaseNumber) {
        this.testCaseNumber = testCaseNumber;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }

    public String getActualOutput() {
        return actualOutput;
    }

    public void setActualOutput(String actualOutput) {
        this.actualOutput = actualOutput;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
}
