package com.remoteprep.execution;

/**
 * Encapsulates a candidate code execution request.
 */
public class ExecutionRequest {

    private String sourceCode;
    private String language;
    private String stdin;
    private Long timeoutMs;

    public ExecutionRequest() {
    }

    public ExecutionRequest(String sourceCode, String language, String stdin) {
        this.sourceCode = sourceCode;
        this.language = language;
        this.stdin = stdin;
    }

    public ExecutionRequest(String sourceCode, String language, String stdin, Long timeoutMs) {
        this.sourceCode = sourceCode;
        this.language = language;
        this.stdin = stdin;
        this.timeoutMs = timeoutMs;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getStdin() {
        return stdin;
    }

    public void setStdin(String stdin) {
        this.stdin = stdin;
    }

    public Long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(Long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }
}
