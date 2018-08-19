package com.github.wei.jtrace.api.transform.matcher;

import java.util.Set;

import com.github.wei.jtrace.api.clazz.ClassDescriber;
import com.github.wei.jtrace.api.clazz.MethodDescriber;

public interface IMatchedListener {
	
	void matched(ClassDescriber classDescriber, Set<MethodDescriber> matchedMethods);
	
}
