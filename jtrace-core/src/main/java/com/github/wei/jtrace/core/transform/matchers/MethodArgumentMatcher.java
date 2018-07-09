package com.github.wei.jtrace.core.transform.matchers;

import com.github.wei.jtrace.core.clazz.MethodDescriber;
import com.github.wei.jtrace.core.util.StringUtil;

public class MethodArgumentMatcher implements IMethodMatcher{
	
	private String name;
	private int args;
	public MethodArgumentMatcher(String name, int args) {
		this.name = name;
		this.args = args;
	}
	
	@Override
	public boolean match(MethodDescriber target) {
		return StringUtil.match(name, target.getName()) && target.getArgumentTypes() != null && target.getArgumentTypes().length == args;
	}

}
