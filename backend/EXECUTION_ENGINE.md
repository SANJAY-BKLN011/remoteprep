# RemotePrep Local Code Execution Engine Foundation (Phase 10)

## Overview
RemotePrep is designed for offline college campus computer labs. The code execution engine provides a modular, controlled backend subsystem for compiling and executing candidate programs locally on the machine hosting the Spring Boot service without relying on internet connectivity or external cloud services.

> **CRITICAL SECURITY NOTICE**:
> **This execution engine is not a security sandbox.**
> While it enforces strict execution timeouts, caps cumulative process output, utilizes dedicated temporary workspaces, and strips environment credentials, arbitrary native code execution (especially C and C++) can still pose host-level risks. Production deployments in unvetted or untrusted environments must layer operating-system-level confinement (e.g. Linux cgroups, unprivileged user sandboxes, or container isolation).

---

## 1. Execution Flow & Architecture

```
ExecutionRequest (sourceCode, language, stdin, timeoutMs)
    │
    ▼
CodeExecutionService / CodeExecutionServiceImpl
    │
    ├─► Validates language & input
    ├─► Creates isolated temp directory: <tempDir>/exec-<UUID>
    │
    ▼
ExecutionStrategy (selected by normalized language: JAVA, CPP, C, PYTHON)
    │
    ├─► Writes source file (Main.java / solution.cpp / solution.c / solution.py)
    ├─► Compiles (javac / g++ / gcc) if required
    │
    ▼
ProcessRunner (Controlled ProcessBuilder Execution)
    │
    ├─► Direct execution without shell wrappers (no cmd.exe / powershell / bash)
    ├─► Strips sensitive environment variables (DB_PASSWORD, MYSQL_PWD, etc.)
    ├─► Feeds stdin asynchronously
    ├─► Asynchronously streams stdout & stderr with byte limits
    ├─► Enforces execution timeout via process.waitFor(timeoutMs, TimeUnit.MILLISECONDS)
    ├─► Destroys unresponsive processes forcibly on timeout or buffer flood
    │
    ▼
ExecutionResult (status, stdout, stderr, exitCode, executionTimeMs, timedOut)
    │
    ▼
finally {
    deleteWorkspaceSafely(); // Reliable recursive cleanup
}
```

---

## 2. Supported Languages

| Language | DB Code | Source Filename | Compiler / Runtime Command | Compilation Step |
|---|---|---|---|---|
| **Java** | `JAVA` | `Main.java` | `javac Main.java` $\rightarrow$ `java Main` | Yes (`javac`) |
| **C++** | `CPP` | `solution.cpp` | `g++ -O2 solution.cpp -o solution.exe` $\rightarrow$ `./solution.exe` | Yes (`g++`) |
| **C** | `C` | `solution.c` | `gcc -O2 solution.c -o solution.exe` $\rightarrow$ `./solution.exe` | Yes (`gcc`) |
| **Python** | `PYTHON` | `solution.py` | `python solution.py` | No (Interpreted) |

*Language values are normalized case-insensitively (`java`, `Java`, `JAVA` $\rightarrow$ `JAVA`).*

---

## 3. Configuration Properties

Properties can be configured in `application.properties` or overridden via environment variables:

| Property | Default Value | Description |
|---|---|---|
| `remoteprep.execution.java-compiler` | `javac` | Path or executable name for Java compiler |
| `remoteprep.execution.java-runtime` | `java` | Path or executable name for Java runtime |
| `remoteprep.execution.c-compiler` | `gcc` | Path or executable name for C compiler |
| `remoteprep.execution.cpp-compiler` | `g++` | Path or executable name for C++ compiler |
| `remoteprep.execution.python-runtime` | `python` | Path or executable name for Python interpreter |
| `remoteprep.execution.timeout-ms` | `5000` | Maximum program execution time in milliseconds |
| `remoteprep.execution.compile-timeout-ms` | `10000` | Maximum compiler execution time in milliseconds |
| `remoteprep.execution.max-output-bytes` | `1048576` | Maximum allowed stdout/stderr volume (1 MB) |
| `remoteprep.execution.temp-dir` | *System tmpdir* | Base directory for execution workspaces |

---

## 4. Resource & Safety Controls

1. **No Shell Invocations**: Commands are passed directly as array arguments to Java's `ProcessBuilder` (e.g. `["java", "Main"]`), preventing shell-injection attacks (`cmd /c`, `sh -c`).
2. **Environment Sanitization**: Sensitive environment variables (`DB_PASSWORD`, `DB_USERNAME`, `MYSQL_PWD`, `SPRING_DATASOURCE_PASSWORD`) are stripped before spawning child processes.
3. **Execution Timeout**: Candidate processes exceeding `timeout-ms` (default 5000 ms) are forcibly killed with `process.destroyForcibly()`, returning `TIME_LIMIT_EXCEEDED`.
4. **Output Volume Cap**: Processes emitting more than `max-output-bytes` (default 1 MB) are terminated immediately with `OUTPUT_LIMIT_EXCEEDED`, preventing heap exhaustion or denial-of-service.
5. **Isolated Workspaces**: Each execution generates a fresh directory (`exec-<UUID>`). A `try ... finally` block ensures complete recursive deletion of generated files upon termination.
