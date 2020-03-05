package com.github.wei.jtrace.api;

public interface IExportClassService {
    boolean exportClass(ClassLoader classLoader, String className);
}
