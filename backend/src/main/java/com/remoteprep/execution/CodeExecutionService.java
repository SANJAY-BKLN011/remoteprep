package com.remoteprep.execution;

/**
 * Core interface for executing candidate code across supported languages.
 */
public interface CodeExecutionService {

    ExecutionResult executeCode(ExecutionRequest request);
}
