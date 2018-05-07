package com.github.wei.jtrace.api.clazz;

import com.github.wei.jtrace.api.exception.ClassFinderException;

public interface IClassFinderManager {
	
	IClassFinder getClassFinder(ClassLoader loader) throws ClassFinderException;
}
