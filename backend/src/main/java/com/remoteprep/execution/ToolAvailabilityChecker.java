package com.remoteprep.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Utility to verify if compilers or runtimes (javac, gcc, python, etc.) are available in PATH.
 */
public final class ToolAvailabilityChecker {

    private static final Logger log = LoggerFactory.getLogger(ToolAvailabilityChecker.class);
    private static final ConcurrentHashMap<String, Boolean> CACHE = new ConcurrentHashMap<>();

    private ToolAvailabilityChecker() {
    }

    public static boolean isToolAvailable(String command) {
        if (command == null || command.isBlank()) {
            return false;
        }

        return CACHE.computeIfAbsent(command, cmd -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(cmd, "--version");
                if (cmd.equalsIgnoreCase("javac") || cmd.equalsIgnoreCase("java")) {
                    pb = new ProcessBuilder(cmd, "-version");
                }
                Process process = pb.start();
                boolean finished = process.waitFor(2, TimeUnit.SECONDS);
                if (!finished) {
                    process.destroyForcibly();
                    return false;
                }
                return process.exitValue() == 0;
            } catch (Exception e) {
                log.debug("Tool {} is not available in PATH: {}", cmd, e.getMessage());
                return false;
            }
        });
    }
}
