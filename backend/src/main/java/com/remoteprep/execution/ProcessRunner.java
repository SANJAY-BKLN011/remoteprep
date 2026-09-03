package com.remoteprep.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Controlled process runner for compiling and executing candidate code.
 * Enforces timeout, limits output volume, feeds stdin, strips environment secrets,
 * and uses ProcessBuilder with explicit arguments (no shell invocation).
 */
@Component
public class ProcessRunner {

    private static final Logger log = LoggerFactory.getLogger(ProcessRunner.class);

    private static final List<String> BLOCKED_ENV_VARS = List.of(
            "DB_PASSWORD", "DB_USERNAME", "MYSQL_PWD", "SPRING_DATASOURCE_PASSWORD",
            "SPRING_DATASOURCE_USERNAME", "DATABASE_URL"
    );

    public ProcessOutput run(List<String> command, File workingDir, String stdin, long timeoutMs, int maxOutputBytes) {
        ProcessBuilder pb = new ProcessBuilder(command);
        if (workingDir != null) {
            pb.directory(workingDir);
        }

        // Security: Strip sensitive database credentials and application secrets from the child process
        Map<String, String> env = pb.environment();
        for (String secretVar : BLOCKED_ENV_VARS) {
            env.remove(secretVar);
        }

        long startTime = System.currentTimeMillis();
        Process process = null;
        AtomicBoolean outputExceeded = new AtomicBoolean(false);

        try {
            process = pb.start();

            // Feed stdin if provided
            if (stdin != null && !stdin.isEmpty()) {
                try (OutputStream os = process.getOutputStream()) {
                    os.write(stdin.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                } catch (Exception e) {
                    // Ignore broken pipe if process terminates early
                }
            } else {
                try {
                    process.getOutputStream().close();
                } catch (Exception ignored) {
                }
            }

            // Asynchronously capture stdout and stderr to prevent deadlocks from full pipe buffers
            Process finalProcess = process;
            CompletableFuture<String> stdoutFuture = CompletableFuture.supplyAsync(() ->
                    readStreamWithLimit(finalProcess.getInputStream(), maxOutputBytes, outputExceeded, finalProcess));
            CompletableFuture<String> stderrFuture = CompletableFuture.supplyAsync(() ->
                    readStreamWithLimit(finalProcess.getErrorStream(), maxOutputBytes, outputExceeded, finalProcess));

            boolean finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
            long executionTime = System.currentTimeMillis() - startTime;

            if (!finished) {
                log.warn("Process timed out after {}ms: {}", timeoutMs, command.get(0));
                destroyProcessGracefully(process);
                return new ProcessOutput("", "Time limit exceeded (" + timeoutMs + "ms)", -1, executionTime, true, false);
            }

            String stdout = stdoutFuture.get(1, TimeUnit.SECONDS);
            String stderr = stderrFuture.get(1, TimeUnit.SECONDS);
            int exitCode = process.exitValue();

            if (outputExceeded.get()) {
                return new ProcessOutput(stdout, stderr + "\n[Output limit of " + maxOutputBytes + " bytes exceeded]",
                        exitCode, executionTime, false, true);
            }

            return new ProcessOutput(stdout, stderr, exitCode, executionTime, false, false);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            destroyProcessGracefully(process);
            long executionTime = System.currentTimeMillis() - startTime;
            return new ProcessOutput("", "Execution interrupted", -1, executionTime, false, false);
        } catch (Exception e) {
            destroyProcessGracefully(process);
            long executionTime = System.currentTimeMillis() - startTime;
            return new ProcessOutput("", "Execution error: " + e.getMessage(), -1, executionTime, false, false);
        }
    }

    private String readStreamWithLimit(InputStream is, int maxBytes, AtomicBoolean outputExceeded, Process process) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int read;
            int total = 0;
            while ((read = is.read(buffer)) != -1) {
                if (total + read > maxBytes) {
                    int remaining = maxBytes - total;
                    if (remaining > 0) {
                        baos.write(buffer, 0, remaining);
                    }
                    outputExceeded.set(true);
                    destroyProcessGracefully(process);
                    break;
                }
                baos.write(buffer, 0, read);
                total += read;
            }
            return baos.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    private void destroyProcessGracefully(Process process) {
        if (process != null && process.isAlive()) {
            process.destroy();
            try {
                if (!process.waitFor(200, TimeUnit.MILLISECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException ignored) {
                process.destroyForcibly();
                Thread.currentThread().interrupt();
            }
        }
    }
}
