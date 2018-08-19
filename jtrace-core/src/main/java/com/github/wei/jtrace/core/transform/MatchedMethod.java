package com.github.wei.jtrace.core.transform;

import com.github.wei.jtrace.api.clazz.MethodDescriber;
import com.github.wei.jtrace.api.transform.matcher.MatcherContext;

public class MatchedMethod {
	private MatcherContext context = new MatcherContext();
	private MethodDescriber methodDescriber;
	
	public MatchedMethod(MethodDescriber methodDescriber) {
		this.methodDescriber = methodDescriber;
	}
	
	public MatchedMethod(MethodDescriber methodDescriber, MatcherContext context) {
		this.methodDescriber = methodDescriber;
		this.context.merge(context);
	}
	
	public MatcherContext getContext() {
		return context;
	}
	
	public MethodDescriber getMethodDescriber() {
		return methodDescriber;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((methodDescriber == null) ? 0 : methodDescriber.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MatchedMethod other = (MatchedMethod) obj;
		if (methodDescriber == null) {
			if (other.methodDescriber != null)
				return false;
		} else if (!methodDescriber.equals(other.methodDescriber))
			return false;
		return true;
	}
	
	
}
