package com.github.wei.jtrace.api.transform.matcher;

import com.github.wei.jtrace.api.clazz.ClassDescriber;

public class ClassAnnotationMatcher extends AbstractClassMatcher {
	private String annotation;
	public ClassAnnotationMatcher(String annotation) {
		this.annotation = annotation;
	}
	
	@Override
	public boolean isMatchSubClass() {
		return true;
	}

	@Override
	protected boolean match(ClassDescriber classDescr) {
		String[] anns = classDescr.getAnnotations();
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
