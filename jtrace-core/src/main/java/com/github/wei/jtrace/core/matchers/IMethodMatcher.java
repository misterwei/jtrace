package com.github.wei.jtrace.core.matchers;

import com.github.wei.jtrace.core.clazz.MethodDescriber;

public interface IMethodMatcher {

	boolean match(MethodDescriber target);
}
