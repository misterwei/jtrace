package com.github.wei.jtrace.core.test;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.jar.JarFile;

public class TestInstrumentation implements Instrumentation {

	@Override
	public void addTransformer(ClassFileTransformer transformer, boolean canRetransform) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addTransformer(ClassFileTransformer transformer) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean removeTransformer(ClassFileTransformer transformer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRetransformClassesSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void retransformClasses(Class<?>... classes) throws UnmodifiableClassException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isRedefineClassesSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void redefineClasses(ClassDefinition... definitions)
			throws ClassNotFoundException, UnmodifiableClassException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isModifiableClass(Class<?> theClass) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Class[] getAllLoadedClasses() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class[] getInitiatedClasses(ClassLoader loader) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getObjectSize(Object objectToSize) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void appendToBootstrapClassLoaderSearch(JarFile jarfile) {
		// TODO Auto-generated method stub

	}

	@Override
	public void appendToSystemClassLoaderSearch(JarFile jarfile) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isNativeMethodPrefixSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setNativeMethodPrefix(ClassFileTransformer transformer, String prefix) {
		// TODO Auto-generated method stub

	}

}
