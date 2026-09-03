package com.remoteprep.execution;

/**
 * Execution outcome status of a candidate program.
 */
public enum ExecutionStatus {
    SUCCESS,
    COMPILATION_ERROR,
    RUNTIME_ERROR,
    TIME_LIMIT_EXCEEDED,
    OUTPUT_LIMIT_EXCEEDED,
    EXECUTION_ERROR
}
