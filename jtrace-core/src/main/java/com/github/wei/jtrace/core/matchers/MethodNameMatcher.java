package com.github.wei.jtrace.core.matchers;

import com.github.wei.jtrace.core.clazz.MethodDescriber;
import com.github.wei.jtrace.core.util.StringUtil;

public class MethodNameMatcher implements IMethodMatcher{

	private String name;
	public MethodNameMatcher(String name) {
		this.name = name;
	}
	
	@Override
	public boolean match(MethodDescriber target) {
		return StringUtil.match(name, target.getName());
	}

}
