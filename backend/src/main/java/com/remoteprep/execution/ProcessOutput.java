package com.remoteprep.execution;

/**
 * Result of a low-level process execution.
 */
public class ProcessOutput {

    private final String stdout;
    private final String stderr;
    private final int exitCode;
    private final long executionTimeMs;
    private final boolean timedOut;
    private final boolean outputLimitExceeded;

    public ProcessOutput(String stdout, String stderr, int exitCode, long executionTimeMs,
                         boolean timedOut, boolean outputLimitExceeded) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.exitCode = exitCode;
        this.executionTimeMs = executionTimeMs;
        this.timedOut = timedOut;
        this.outputLimitExceeded = outputLimitExceeded;
    }

    public String getStdout() {
        return stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public int getExitCode() {
        return exitCode;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public boolean isTimedOut() {
        return timedOut;
    }

    public boolean isOutputLimitExceeded() {
        return outputLimitExceeded;
    }
}
