package com.remoteprep.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Execution strategy for Python programs.
 * Directly runs solution.py using configured Python interpreter.
 */
@Component
public class PythonExecutionStrategy implements ExecutionStrategy {

    private static final Logger log = LoggerFactory.getLogger(PythonExecutionStrategy.class);

    private final ProcessRunner processRunner;
    private final ExecutionProperties properties;

    public PythonExecutionStrategy(ProcessRunner processRunner, ExecutionProperties properties) {
        this.processRunner = processRunner;
        this.properties = properties;
    }

    @Override
    public boolean supports(String language) {
        return "PYTHON".equalsIgnoreCase(language);
    }

    @Override
    public ExecutionResult execute(ExecutionRequest request, Path workspace) throws Exception {
        Path sourceFile = workspace.resolve("solution.py");
        Files.writeString(sourceFile, request.getSourceCode());

        long timeoutMs = request.getTimeoutMs() != null ? request.getTimeoutMs() : properties.getTimeoutMs();

        log.debug("Executing Python script in {}", workspace);
        ProcessOutput runOut = processRunner.run(
                List.of(properties.getPythonRuntime(), "solution.py"),
                workspace.toFile(),
                request.getStdin(),
                timeoutMs,
                properties.getMaxOutputBytes()
        );

        if (runOut.isTimedOut()) {
            return ExecutionResult.timeout(runOut.getExecutionTimeMs(), false);
        }
        if (runOut.isOutputLimitExceeded()) {
            return ExecutionResult.outputLimitExceeded(runOut.getStdout(), runOut.getStderr(), runOut.getExecutionTimeMs());
        }

        ExecutionStatus status = (runOut.getExitCode() == 0) ? ExecutionStatus.SUCCESS : ExecutionStatus.RUNTIME_ERROR;
        return new ExecutionResult(
                status,
                runOut.getStdout(),
                runOut.getStderr(),
                runOut.getExitCode(),
                runOut.getExecutionTimeMs(),
                false,
                false,
                null
        );
    }
}
