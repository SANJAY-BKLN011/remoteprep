package com.remoteprep.execution;

/**
 * Result model produced by the execution engine.
 */
public class ExecutionResult {

    private ExecutionStatus status;
    private String stdout;
    private String stderr;
    private Integer exitCode;
    private Long executionTimeMs;
    private boolean timedOut;
    private boolean compilationRequired;
    private Boolean compilationSucceeded;

    public ExecutionResult() {
    }

    public ExecutionResult(ExecutionStatus status, String stdout, String stderr, Integer exitCode,
                           Long executionTimeMs, boolean timedOut, boolean compilationRequired,
                           Boolean compilationSucceeded) {
        this.status = status;
        this.stdout = stdout != null ? stdout : "";
        this.stderr = stderr != null ? stderr : "";
        this.exitCode = exitCode;
        this.executionTimeMs = executionTimeMs != null ? executionTimeMs : 0L;
        this.timedOut = timedOut;
        this.compilationRequired = compilationRequired;
        this.compilationSucceeded = compilationSucceeded;
    }

    public static ExecutionResult compilationError(String stderr, Long timeMs) {
        return new ExecutionResult(ExecutionStatus.COMPILATION_ERROR, "", stderr, -1, timeMs, false, true, false);
    }

    public static ExecutionResult timeout(Long timeMs, boolean compilationRequired) {
        return new ExecutionResult(ExecutionStatus.TIME_LIMIT_EXCEEDED, "", "Execution timed out.", -1, timeMs, true, compilationRequired, true);
    }

    public static ExecutionResult outputLimitExceeded(String stdout, String stderr, Long timeMs) {
        return new ExecutionResult(ExecutionStatus.OUTPUT_LIMIT_EXCEEDED, stdout, stderr, -1, timeMs, false, false, true);
    }

    public static ExecutionResult error(String message) {
        return new ExecutionResult(ExecutionStatus.EXECUTION_ERROR, "", message, -1, 0L, false, false, false);
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public boolean isTimedOut() {
        return timedOut;
    }

    public void setTimedOut(boolean timedOut) {
        this.timedOut = timedOut;
    }

    public boolean isCompilationRequired() {
        return compilationRequired;
    }

    public void setCompilationRequired(boolean compilationRequired) {
        this.compilationRequired = compilationRequired;
    }

    public Boolean getCompilationSucceeded() {
        return compilationSucceeded;
    }

    public void setCompilationSucceeded(Boolean compilationSucceeded) {
        this.compilationSucceeded = compilationSucceeded;
    }
}
