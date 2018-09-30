package com.github.wei.jtrace.core.transform.matchers;

import com.github.wei.jtrace.api.clazz.MethodDescriber;
import com.github.wei.jtrace.api.transform.matcher.IMethodMatcher;

public class MethodAndMatcher implements IMethodMatcher {
	
	private IMethodMatcher[] matchers = null;
	public MethodAndMatcher(IMethodMatcher ...matchers) {
		this.matchers = matchers;
	}
	
	@Override
	public boolean match(MethodDescriber target) {
		for(IMethodMatcher matcher: matchers) {
			if(matcher ==null || !matcher.match(target)) {
				return false;
			}
		}
		return true;
	}

}
