package com.remoteprep.dto;

/**
 * Multi-language starter template DTO for Java, C++, C, and Python.
 */
public class StarterCodeDto {

    private String java;
    private String cpp;
    private String c;
    private String python;

    public StarterCodeDto() {
    }

    public StarterCodeDto(String java, String cpp, String c, String python) {
        this.java = java;
        this.cpp = cpp;
        this.c = c;
        this.python = python;
    }

    public String getJava() {
        return java;
    }

    public void setJava(String java) {
        this.java = java;
    }

    public String getCpp() {
        return cpp;
    }

    public void setCpp(String cpp) {
        this.cpp = cpp;
    }

    public String getC() {
        return c;
    }

    public void setC(String c) {
        this.c = c;
    }

    public String getPython() {
        return python;
    }

    public void setPython(String python) {
        this.python = python;
    }
}
