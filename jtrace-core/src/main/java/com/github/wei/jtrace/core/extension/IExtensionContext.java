package com.github.wei.jtrace.core.extension;

public interface IExtensionContext {
	Class<?> loadClass(String className) throws ClassNotFoundException;
	
	String getJarName();
	
	ExtensionJarInfo getJarInfo();
}
