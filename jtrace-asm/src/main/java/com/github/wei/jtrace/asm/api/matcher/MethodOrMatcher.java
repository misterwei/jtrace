package com.github.wei.jtrace.asm.api.matcher;

import org.objectweb.asm.tree.MethodNode;

public class MethodOrMatcher implements IMethodMatcher {
	
	private IMethodMatcher[] matchers = null;
	public MethodOrMatcher(IMethodMatcher ...matchers) {
		this.matchers = matchers;
	}
	
	@Override
	public boolean match(MethodNode target) {
		for(IMethodMatcher matcher: matchers) {
			if(matcher != null && matcher.match(target)) {
				return true;
			}
		}
		return false;
	}

}
