package com.github.wei.jtrace.api.transform.matcher;

public interface IMethodMatcherWithContext extends IMethodMatcher{
	
	MatcherContext getContext();
}
