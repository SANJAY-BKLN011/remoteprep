package com.remoteprep.execution;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configurable properties for the local DSA code execution engine.
 */
@Component
@ConfigurationProperties(prefix = "remoteprep.execution")
public class ExecutionProperties {

    private String javaCompiler = "javac";
    private String javaRuntime = "java";
    private String cCompiler = "gcc";
    private String cppCompiler = "g++";
    private String pythonRuntime = "python";
    private long timeoutMs = 5000L;
    private long compileTimeoutMs = 10000L;
    private int maxOutputBytes = 1048576; // 1 MB
    private String tempDir = "";

    public String getJavaCompiler() {
        return javaCompiler;
    }

    public void setJavaCompiler(String javaCompiler) {
        this.javaCompiler = javaCompiler;
    }

    public String getJavaRuntime() {
        return javaRuntime;
    }

    public void setJavaRuntime(String javaRuntime) {
        this.javaRuntime = javaRuntime;
    }

    public String getCCompiler() {
        return cCompiler;
    }

    public void setCCompiler(String cCompiler) {
        this.cCompiler = cCompiler;
    }

    public String getCppCompiler() {
        return cppCompiler;
    }

    public void setCppCompiler(String cppCompiler) {
        this.cppCompiler = cppCompiler;
    }

    public String getPythonRuntime() {
        return pythonRuntime;
    }

    public void setPythonRuntime(String pythonRuntime) {
        this.pythonRuntime = pythonRuntime;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public long getCompileTimeoutMs() {
        return compileTimeoutMs;
    }

    public void setCompileTimeoutMs(long compileTimeoutMs) {
        this.compileTimeoutMs = compileTimeoutMs;
    }

    public int getMaxOutputBytes() {
        return maxOutputBytes;
    }

    public void setMaxOutputBytes(int maxOutputBytes) {
        this.maxOutputBytes = maxOutputBytes;
    }

    public String getTempDir() {
        return tempDir;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }
}
