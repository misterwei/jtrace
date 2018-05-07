package com.github.wei.jtrace.api.matcher;

import java.util.Arrays;

import com.github.wei.jtrace.api.clazz.ClassDescriber;

public class InterfaceClassMatcher extends AbstractClassMatcher{

	private String[] className;
	public InterfaceClassMatcher(String... className) {
		this.className = className;
		Arrays.sort(className);
	}
	
	@Override
	public boolean match(ClassDescriber descr) {
		return descr.getModifier().contains("interface") && Arrays.binarySearch(className, descr.getName()) > -1;
	}

	public boolean isMatchSubClass() {
		return true;
	}

}
