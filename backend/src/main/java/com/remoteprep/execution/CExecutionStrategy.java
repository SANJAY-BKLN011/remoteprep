package com.remoteprep.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Execution strategy for C programs.
 * Compiles solution.c with gcc and executes the generated binary.
 */
@Component
public class CExecutionStrategy implements ExecutionStrategy {

    private static final Logger log = LoggerFactory.getLogger(CExecutionStrategy.class);

    private final ProcessRunner processRunner;
    private final ExecutionProperties properties;

    public CExecutionStrategy(ProcessRunner processRunner, ExecutionProperties properties) {
        this.processRunner = processRunner;
        this.properties = properties;
    }

    @Override
    public boolean supports(String language) {
        return "C".equalsIgnoreCase(language);
    }

    @Override
    public ExecutionResult execute(ExecutionRequest request, Path workspace) throws Exception {
        Path sourceFile = workspace.resolve("solution.c");
        Files.writeString(sourceFile, request.getSourceCode());

        String binaryName = System.getProperty("os.name").toLowerCase().contains("win") ? "solution.exe" : "solution";
        long timeoutMs = request.getTimeoutMs() != null ? request.getTimeoutMs() : properties.getTimeoutMs();

        // 1. Compile C source
        log.debug("Compiling C source in {}", workspace);
        ProcessOutput compileOut = processRunner.run(
                List.of(properties.getCCompiler(), "-O2", "solution.c", "-o", binaryName),
                workspace.toFile(),
                null,
                properties.getCompileTimeoutMs(),
                properties.getMaxOutputBytes()
        );

        if (compileOut.getExitCode() != 0) {
            String errorMsg = !compileOut.getStderr().isBlank() ? compileOut.getStderr() : compileOut.getStdout();
            return ExecutionResult.compilationError(errorMsg, compileOut.getExecutionTimeMs());
        }

        // 2. Execute compiled binary
        Path binaryPath = workspace.resolve(binaryName);
        ProcessOutput runOut = processRunner.run(
                List.of(binaryPath.toAbsolutePath().toString()),
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
