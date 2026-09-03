package com.remoteprep.execution;

import java.nio.file.Path;

/**
 * Interface defining language-specific compilation and execution strategy.
 */
public interface ExecutionStrategy {

    boolean supports(String language);

    ExecutionResult execute(ExecutionRequest request, Path workspace) throws Exception;
}
