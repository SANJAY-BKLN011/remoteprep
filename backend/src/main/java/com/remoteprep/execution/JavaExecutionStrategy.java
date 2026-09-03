package com.remoteprep.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Execution strategy for Java programs.
 * Compiles Main.java via javac and executes Main via java.
 */
@Component
public class JavaExecutionStrategy implements ExecutionStrategy {

    private static final Logger log = LoggerFactory.getLogger(JavaExecutionStrategy.class);

    private final ProcessRunner processRunner;
    private final ExecutionProperties properties;

    public JavaExecutionStrategy(ProcessRunner processRunner, ExecutionProperties properties) {
        this.processRunner = processRunner;
        this.properties = properties;
    }

    @Override
    public boolean supports(String language) {
        return "JAVA".equalsIgnoreCase(language);
    }

    @Override
    public ExecutionResult execute(ExecutionRequest request, Path workspace) throws Exception {
        Path sourceFile = workspace.resolve("Main.java");
        Files.writeString(sourceFile, request.getSourceCode());

        long timeoutMs = request.getTimeoutMs() != null ? request.getTimeoutMs() : properties.getTimeoutMs();

        // 1. Compile Main.java
        log.debug("Compiling Java source in {}", workspace);
        ProcessOutput compileOut = processRunner.run(
                List.of(properties.getJavaCompiler(), "Main.java"),
                workspace.toFile(),
                null,
                properties.getCompileTimeoutMs(),
                properties.getMaxOutputBytes()
        );

        if (compileOut.getExitCode() != 0) {
            String errorMsg = !compileOut.getStderr().isBlank() ? compileOut.getStderr() : compileOut.getStdout();
            return ExecutionResult.compilationError(errorMsg, compileOut.getExecutionTimeMs());
        }

        // 2. Execute java Main
        log.debug("Executing Java class in {}", workspace);
        ProcessOutput runOut = processRunner.run(
                List.of(properties.getJavaRuntime(), "Main"),
                workspace.toFile(),
                request.getStdin(),
                timeoutMs,
                properties.getMaxOutputBytes()
        );

        return toExecutionResult(runOut, true);
    }

    private ExecutionResult toExecutionResult(ProcessOutput out, boolean compilationRequired) {
        if (out.isTimedOut()) {
            return ExecutionResult.timeout(out.getExecutionTimeMs(), compilationRequired);
        }
        if (out.isOutputLimitExceeded()) {
            return ExecutionResult.outputLimitExceeded(out.getStdout(), out.getStderr(), out.getExecutionTimeMs());
        }

        ExecutionStatus status = (out.getExitCode() == 0) ? ExecutionStatus.SUCCESS : ExecutionStatus.RUNTIME_ERROR;
        return new ExecutionResult(
                status,
                out.getStdout(),
                out.getStderr(),
                out.getExitCode(),
                out.getExecutionTimeMs(),
                false,
                compilationRequired,
                true
        );
    }
}
