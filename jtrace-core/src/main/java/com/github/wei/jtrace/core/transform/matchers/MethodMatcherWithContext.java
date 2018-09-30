package com.github.wei.jtrace.core.transform.matchers;

import java.util.Map;

import com.github.wei.jtrace.api.clazz.MethodDescriber;
import com.github.wei.jtrace.api.transform.matcher.IMethodMatcher;
import com.github.wei.jtrace.api.transform.matcher.IMethodMatcherWithContext;
import com.github.wei.jtrace.api.transform.matcher.MatcherContext;

public class MethodMatcherWithContext implements IMethodMatcherWithContext{
	private IMethodMatcher matcher;
	private MatcherContext context = new MatcherContext();
	
	public MethodMatcherWithContext(IMethodMatcher matcher) {
		this.matcher = matcher;
	}

	public MethodMatcherWithContext(IMethodMatcher matcher, Map<String, Object> params) {
		this.matcher = matcher;
		this.context.putAll(params);
	}
	
	public MatcherContext getContext() {
		return context;
	}
	
	@Override
	public boolean match(MethodDescriber target) {
		return matcher.match(target);
	}
}
