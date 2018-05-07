package com.github.wei.jtrace.api.config;

import java.util.Set;

public interface IConfig {

	int getInt(String path);

    long getLong(String path);

    double getDouble(String path);

    String getString(String path);
    
    boolean getBoolean(String path);
    
    int getInt(String path, int defaultValue);

    long getLong(String path, long  defaultValue);

    double getDouble(String path, double  defaultValue);

    String getString(String path, String  defaultValue);
    
    boolean getBoolean(String path, boolean  defaultValue);
    
    Set<String> keySet();
}
