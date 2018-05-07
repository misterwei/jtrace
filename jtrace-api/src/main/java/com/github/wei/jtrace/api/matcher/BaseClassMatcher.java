package com.github.wei.jtrace.api.matcher;

import java.util.Arrays;

import com.github.wei.jtrace.api.clazz.ClassDescriber;

public class BaseClassMatcher extends AbstractClassMatcher{

	private String[] className;
	public BaseClassMatcher(String... className) {
		this.className = className;
		Arrays.sort(className);
	}
	
	@Override
	public boolean match(ClassDescriber descr) {
		return Arrays.binarySearch(className, descr.getName()) > -1;
	}

	public boolean isMatchSubClass() {
		return true;
	}
}
