package com.github.wei.jtrace.core.transform.matchers;

import java.util.List;

import com.github.wei.jtrace.api.clazz.IClassDescriberTree;
import com.github.wei.jtrace.api.exception.ClassMatchException;
import com.github.wei.jtrace.api.transform.matcher.IClassMatcher;

public class OrClassMatcher implements IClassMatcher{

	private IClassMatcher[] matchers;
	
	public OrClassMatcher(IClassMatcher ...matchers) {
		this.matchers = matchers;
	}
	
	public OrClassMatcher(List<? extends IClassMatcher> matchers) {
		this.matchers = matchers.toArray(new IClassMatcher[matchers.size()]);
	}
	
	@Override
	public boolean matchClass(IClassDescriberTree descr) throws ClassMatchException {
		for(IClassMatcher matcher : matchers) {
			if(matcher.matchClass(descr)) {
				return true;
			}
		}
		return false;
	}

}
