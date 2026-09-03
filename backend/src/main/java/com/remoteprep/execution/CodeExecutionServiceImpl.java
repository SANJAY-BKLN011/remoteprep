package com.remoteprep.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of CodeExecutionService.
 * Coordinates workspace creation, language strategy selection, execution,
 * and reliable cleanup.
 */
@Service
public class CodeExecutionServiceImpl implements CodeExecutionService {

    private static final Logger log = LoggerFactory.getLogger(CodeExecutionServiceImpl.class);

    private final List<ExecutionStrategy> strategies;
    private final ExecutionProperties properties;

    public CodeExecutionServiceImpl(List<ExecutionStrategy> strategies, ExecutionProperties properties) {
        this.strategies = strategies;
        this.properties = properties;
    }

    @Override
    public ExecutionResult executeCode(ExecutionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("ExecutionRequest cannot be null");
        }
        if (request.getSourceCode() == null || request.getSourceCode().isBlank()) {
            throw new IllegalArgumentException("Source code cannot be blank");
        }
        if (request.getLanguage() == null || request.getLanguage().isBlank()) {
            throw new IllegalArgumentException("Language must be specified");
        }

        String normalizedLang = request.getLanguage().trim().toUpperCase();
        ExecutionStrategy strategy = strategies.stream()
                .filter(s -> s.supports(normalizedLang))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported language: " + request.getLanguage() +
                        ". Supported languages are: JAVA, CPP, C, PYTHON"));

        String executionId = UUID.randomUUID().toString();
        Path workspace = null;

        try {
            workspace = createWorkspace(executionId);
            log.info("Execution started [id={}, lang={}]", executionId, normalizedLang);

            ExecutionResult result = strategy.execute(request, workspace);
            log.info("Execution completed [id={}, status={}, time={}ms]",
                    executionId, result.getStatus(), result.getExecutionTimeMs());
            return result;

        } catch (Exception e) {
            log.error("Execution failure for id={}: {}", executionId, e.getMessage());
            return ExecutionResult.error("Execution failed: " + e.getMessage());
        } finally {
            if (workspace != null) {
                deleteWorkspaceSafely(workspace, executionId);
            }
        }
    }

    private Path createWorkspace(String executionId) throws IOException {
        Path baseDir;
        if (properties.getTempDir() != null && !properties.getTempDir().isBlank()) {
            baseDir = Paths.get(properties.getTempDir());
        } else {
            baseDir = Paths.get(System.getProperty("java.io.tmpdir"), "remoteprep-executions");
        }

        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);
        }

        Path workspace = baseDir.resolve("exec-" + executionId);
        Files.createDirectories(workspace);
        return workspace;
    }

    private void deleteWorkspaceSafely(Path workspace, String executionId) {
        try {
            if (Files.exists(workspace)) {
                Files.walkFileTree(workspace, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.deleteIfExists(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.deleteIfExists(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
                log.debug("Cleaned up workspace for execution id={}", executionId);
            }
        } catch (Exception e) {
            log.warn("Failed to delete workspace {} for id={}: {}", workspace, executionId, e.getMessage());
        }
    }
}
