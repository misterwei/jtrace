package com.github.wei.jtrace.core.transform.matchers;

import com.github.wei.jtrace.api.clazz.MethodDescriber;
import com.github.wei.jtrace.api.transform.matcher.IMethodMatcher;

public class MethodAnnotationMatcher implements IMethodMatcher{
	private String annotation;
	public MethodAnnotationMatcher(String annotation) {
		this.annotation = annotation;
	}
	
	@Override
	public boolean match(MethodDescriber target) {
		String[] anns = target.getAnnotations();
		if(anns == null) {
			return false;
		}
		
		for(String ann : anns) {
			if(ann.equals(annotation)) {
				return true;
			}
		}
		return false;
	}

}
