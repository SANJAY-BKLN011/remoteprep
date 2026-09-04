package com.remoteprep.service;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dedicated output comparison component for judging DSA code submissions.
 * Normalizes line breaks (CRLF/CR to LF), strips trailing whitespace from lines,
 * and strips overall leading/trailing whitespace before performing exact equality comparison.
 * Never uses substring matching.
 */
@Component
public class DsaOutputComparator {

    /**
     * Compares actual stdout against expected output.
     *
     * @param expected the expected output defined for the test case
     * @param actual   the actual stdout produced by the candidate program
     * @return true if normalized strings match exactly, false otherwise
     */
    public boolean matches(String expected, String actual) {
        String normExpected = normalize(expected);
        String normActual = normalize(actual);
        return normExpected.equals(normActual);
    }

    /**
     * Normalizes output by:
     * 1. Converting CRLF and CR to LF (\n)
     * 2. Stripping trailing whitespace from each line
     * 3. Stripping overall leading and trailing whitespace
     *
     * @param output raw output string
     * @return normalized string
     */
    public String normalize(String output) {
        if (output == null) {
            return "";
        }

        // 1. Standardize line endings to \n
        String standardized = output.replace("\r\n", "\n").replace('\r', '\n');

        // 2. Strip trailing whitespace from each line
        String[] lines = standardized.split("\n", -1);
        List<String> trimmedLines = Arrays.stream(lines)
                .map(String::stripTrailing)
                .collect(Collectors.toList());

        String joined = String.join("\n", trimmedLines);

        // 3. Normalize overall leading and trailing whitespace
        return joined.strip();
    }
}
