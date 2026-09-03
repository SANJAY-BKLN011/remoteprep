package com.remoteprep;

import com.remoteprep.execution.CodeExecutionService;
import com.remoteprep.execution.ExecutionProperties;
import com.remoteprep.execution.ExecutionRequest;
import com.remoteprep.execution.ExecutionResult;
import com.remoteprep.execution.ExecutionStatus;
import com.remoteprep.execution.ToolAvailabilityChecker;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CodeExecutionTests {

    @Autowired
    private CodeExecutionService codeExecutionService;

    @Autowired
    private ExecutionProperties executionProperties;

    @Test
    @DisplayName("Test 1: JAVA successful execution")
    void testJavaSuccessfulExecution() {
        String code = "public class Main { public static void main(String[] args) { System.out.println(\"Hello Java!\"); } }";
        ExecutionResult result = codeExecutionService.executeCode(new ExecutionRequest(code, "JAVA", null));

        assertEquals(ExecutionStatus.SUCCESS, result.getStatus());
        assertEquals("Hello Java!\n", result.getStdout().replace("\r\n", "\n"));
        assertEquals(0, result.getExitCode());
        assertTrue(result.getExecutionTimeMs() >= 0);
        assertTrue(result.isCompilationRequired());
        assertTrue(result.getCompilationSucceeded());
    }

    @Test
    @DisplayName("Test 2: PYTHON successful execution")
    void testPythonSuccessfulExecution() {
        Assumptions.assumeTrue(ToolAvailabilityChecker.isToolAvailable(executionProperties.getPythonRuntime()),
                "Python is not installed or available in PATH");

        String code = "print('Hello Python!')";
        ExecutionResult result = codeExecutionService.executeCode(new ExecutionRequest(code, "PYTHON", null));

        assertEquals(ExecutionStatus.SUCCESS, result.getStatus());
        assertEquals("Hello Python!\n", result.getStdout().replace("\r\n", "\n"));
        assertEquals(0, result.getExitCode());
        assertFalse(result.isCompilationRequired());
    }

    @Test
    @DisplayName("Test 3: C successful execution (conditional on gcc)")
    void testCSuccessfulExecution() {
        Assumptions.assumeTrue(ToolAvailabilityChecker.isToolAvailable(executionProperties.getCCompiler()),
                "gcc is not available in PATH");

        String code = "#include <stdio.h>\nint main() { printf(\"Hello C!\\n\"); return 0; }";
        ExecutionResult result = codeExecutionService.executeCode(new ExecutionRequest(code, "C", null));

        assertEquals(ExecutionStatus.SUCCESS, result.getStatus());
        assertEquals("Hello C!\n", result.getStdout().replace("\r\n", "\n"));
        assertEquals(0, result.getExitCode());
    }

    @Test
    @DisplayName("Test 4: CPP successful execution (conditional on g++)")
    void testCppSuccessfulExecution() {
        Assumptions.assumeTrue(ToolAvailabilityChecker.isToolAvailable(executionProperties.getCppCompiler()),
                "g++ is not available in PATH");

        String code = "#include <iostream>\nint main() { std::cout << \"Hello CPP!\" << std::endl; return 0; }";
        ExecutionResult result = codeExecutionService.executeCode(new ExecutionRequest(code, "CPP", null));

        assertEquals(ExecutionStatus.SUCCESS, result.getStatus());
        assertEquals("Hello CPP!\n", result.getStdout().replace("\r\n", "\n"));
        assertEquals(0, result.getExitCode());
    }

    @Test
    @DisplayName("Test 5: JAVA compilation error")
    void testJavaCompilationError() {
        String code = "public class Main { public static void main(String[] args) { syntax_error; } }";
        ExecutionResult result = codeExecutionService.executeCode(new ExecutionRequest(code, "JAVA", null));

        assertEquals(ExecutionStatus.COMPILATION_ERROR, result.getStatus());
        assertTrue(result.isCompilationRequired());
        assertFalse(result.getCompilationSucceeded());
        assertFalse(result.getStderr().isBlank());
    }

    @Test
    @DisplayName("Test 6: C compilation error (conditional on gcc)")
    void testCCompilationError() {
        Assumptions.assumeTrue(ToolAvailabilityChecker.isToolAvailable(executionProperties.getCCompiler()),
                "gcc is not available in PATH");

        String code = "#include <stdio.h>\nint main() { syntax error; return 0; }";
        ExecutionResult result = codeExecutionService.executeCode(new ExecutionRequest(code, "C", null));

        assertEquals(ExecutionStatus.COMPILATION_ERROR, result.getStatus());
        assertFalse(result.getStderr().isBlank());
    }

    @Test
    @DisplayName("Test 7: CPP compilation error (conditional on g++)")
    void testCppCompilationError() {
        Assumptions.assumeTrue(ToolAvailabilityChecker.isToolAvailable(executionProperties.getCppCompiler()),
                "g++ is not available in PATH");

        String code = "#include <iostream>\nint main() { invalid cpp code; return 0; }";
        ExecutionResult result = codeExecutionService.executeCode(new ExecutionRequest(code, "CPP", null));

        assertEquals(ExecutionStatus.COMPILATION_ERROR, result.getStatus());
        assertFalse(result.getStderr().isBlank());
    }

    @Test
    @DisplayName("Test 8: Python syntax error -> RUNTIME_ERROR")
    void testPythonSyntaxError() {
        Assumptions.assumeTrue(ToolAvailabilityChecker.isToolAvailable(executionProperties.getPythonRuntime()),
                "Python is not installed or available in PATH");

        String code = "def invalid syntax:";
        ExecutionResult result = codeExecutionService.executeCode(new ExecutionRequest(code, "PYTHON", null));

        assertEquals(ExecutionStatus.RUNTIME_ERROR, result.getStatus());
        assertNotEquals(0, result.getExitCode());
        assertFalse(result.getStderr().isBlank());
    }

    @Test
    @DisplayName("Test 9: Runtime error (division by zero)")
    void testRuntimeErrorDivisionByZero() {
        String code = "public class Main { public static void main(String[] args) { int a = 10 / 0; } }";
        ExecutionResult result = codeExecutionService.executeCode(new ExecutionRequest(code, "JAVA", null));

        assertEquals(ExecutionStatus.RUNTIME_ERROR, result.getStatus());
        assertNotEquals(0, result.getExitCode());
        assertTrue(result.getStderr().contains("ArithmeticException") || result.getStderr().contains("/ by zero"));
    }

    @Test
    @DisplayName("Test 10: Infinite loop -> TIME_LIMIT_EXCEEDED")
    void testInfiniteLoopTimeout() {
        String code = "public class Main { public static void main(String[] args) { while(true) {} } }";
        ExecutionResult result = codeExecutionService.executeCode(new ExecutionRequest(code, "JAVA", null, 1500L));

        assertEquals(ExecutionStatus.TIME_LIMIT_EXCEEDED, result.getStatus());
        assertTrue(result.isTimedOut());
        assertTrue(result.getExecutionTimeMs() >= 1400L);
    }

    @Test
    @DisplayName("Test 11 & 12: Stdin and Stdout capture")
    void testStdinStdoutCapture() {
        String code = "import java.util.Scanner;\n" +
                      "public class Main {\n" +
                      "    public static void main(String[] args) {\n" +
                      "        Scanner sc = new Scanner(System.in);\n" +
                      "        int a = sc.nextInt();\n" +
                      "        int b = sc.nextInt();\n" +
                      "        System.out.println(a + b);\n" +
                      "    }\n" +
                      "}";
        ExecutionResult result = codeExecutionService.executeCode(new ExecutionRequest(code, "JAVA", "15 25"));

        assertEquals(ExecutionStatus.SUCCESS, result.getStatus());
        assertEquals("40\n", result.getStdout().replace("\r\n", "\n"));
    }

    @Test
    @DisplayName("Test 13: Stderr capture")
    void testStderrCapture() {
        String code = "public class Main { public static void main(String[] args) { System.err.println(\"Diagnostic log\"); } }";
        ExecutionResult result = codeExecutionService.executeCode(new ExecutionRequest(code, "JAVA", null));

        assertEquals(ExecutionStatus.SUCCESS, result.getStatus());
        assertTrue(result.getStderr().contains("Diagnostic log"));
    }

    @Test
    @DisplayName("Test 14: Execution time recorded")
    void testExecutionTimeRecorded() {
        String code = "public class Main { public static void main(String[] args) throws Exception { Thread.sleep(300); } }";
        ExecutionResult result = codeExecutionService.executeCode(new ExecutionRequest(code, "JAVA", null));

        assertEquals(ExecutionStatus.SUCCESS, result.getStatus());
        assertTrue(result.getExecutionTimeMs() >= 250L);
    }

    @Test
    @DisplayName("Test 15: Temporary workspace cleanup")
    void testTemporaryWorkspaceCleanup() {
        Path tempBase = Paths.get(System.getProperty("java.io.tmpdir"), "remoteprep-executions");

        String code = "public class Main { public static void main(String[] args) { System.out.println(\"Clean me\"); } }";
        codeExecutionService.executeCode(new ExecutionRequest(code, "JAVA", null));

        // Ensure no active subdirectories remain leaked
        if (Files.exists(tempBase)) {
            File[] files = tempBase.toFile().listFiles();
            // All workspaces created during execution should have been deleted
            assertNotNull(files);
            // Workspaces are named exec-<UUID> and should be deleted in finally
            for (File f : files) {
                if (f.getName().startsWith("exec-")) {
                    assertFalse(f.exists(), "Workspace directory was not cleaned up: " + f.getAbsolutePath());
                }
            }
        }
    }

    @Test
    @DisplayName("Test 16: Output-size limit enforcement")
    void testOutputSizeLimitEnforcement() {
        // Generates large flood of output
        String code = "public class Main {\n" +
                      "    public static void main(String[] args) {\n" +
                      "        for (int i = 0; i < 500000; i++) {\n" +
                      "            System.out.println(\"Flood flood flood flood flood flood\");\n" +
                      "        }\n" +
                      "    }\n" +
                      "}";
        ExecutionResult result = codeExecutionService.executeCode(new ExecutionRequest(code, "JAVA", null));

        assertTrue(result.getStatus() == ExecutionStatus.OUTPUT_LIMIT_EXCEEDED ||
                   result.getStdout().length() <= executionProperties.getMaxOutputBytes() + 2048,
                "Output size limit must prevent unbounded memory accumulation");
    }

    @Test
    @DisplayName("Test 17: Unsupported language rejected")
    void testUnsupportedLanguageRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                codeExecutionService.executeCode(new ExecutionRequest("code", "RUBY", null)));
        assertThrows(IllegalArgumentException.class, () ->
                codeExecutionService.executeCode(new ExecutionRequest("code", "JAVASCRIPT", null)));
        assertThrows(IllegalArgumentException.class, () ->
                codeExecutionService.executeCode(new ExecutionRequest("code", "KOTLIN", null)));
    }

    @Test
    @DisplayName("Test 18: Case-insensitive language normalization")
    void testCaseInsensitiveLanguageNormalization() {
        String code = "public class Main { public static void main(String[] args) { System.out.println(\"Normalized\"); } }";

        ExecutionResult r1 = codeExecutionService.executeCode(new ExecutionRequest(code, "java", null));
        assertEquals(ExecutionStatus.SUCCESS, r1.getStatus());

        ExecutionResult r2 = codeExecutionService.executeCode(new ExecutionRequest(code, "Java", null));
        assertEquals(ExecutionStatus.SUCCESS, r2.getStatus());

        ExecutionResult r3 = codeExecutionService.executeCode(new ExecutionRequest(code, "JAVA", null));
        assertEquals(ExecutionStatus.SUCCESS, r3.getStatus());
    }

    @Test
    @DisplayName("Test 19: Candidate code cannot alter ProcessBuilder command")
    void testCandidateCodeCannotInjectShellCommands() {
        // Injection attempt in source code should just be compiled as source code, never parsed as shell arguments
        String maliciousCode = "public class Main { public static void main(String[] args) { System.out.println(\"& echo INJECTED\"); } }";
        ExecutionResult result = codeExecutionService.executeCode(new ExecutionRequest(maliciousCode, "JAVA", null));

        assertEquals(ExecutionStatus.SUCCESS, result.getStatus());
        assertEquals("& echo INJECTED\n", result.getStdout().replace("\r\n", "\n"));
    }

    @Test
    @DisplayName("Test 20: Multiple executions do not share workspace")
    void testMultipleExecutionsIsolation() {
        String codeA = "public class Main { public static void main(String[] args) { System.out.println(\"A\"); } }";
        String codeB = "public class Main { public static void main(String[] args) { System.out.println(\"B\"); } }";

        ExecutionResult resA = codeExecutionService.executeCode(new ExecutionRequest(codeA, "JAVA", null));
        ExecutionResult resB = codeExecutionService.executeCode(new ExecutionRequest(codeB, "JAVA", null));

        assertEquals("A\n", resA.getStdout().replace("\r\n", "\n"));
        assertEquals("B\n", resB.getStdout().replace("\r\n", "\n"));
    }
}
