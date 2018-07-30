package com.github.wei.jtrace.api.transform.matcher;

import com.github.wei.jtrace.api.clazz.MethodDescriber;

public interface IMethodMatcher {

	boolean match(MethodDescriber target);
}
