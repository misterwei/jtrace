package com.github.wei.jtrace.api.clazz;

import com.github.wei.jtrace.api.exception.ClassFinderException;

public interface IClassFinder {

	ClassDescriber find(String className) throws ClassFinderException;
}
