package com.github.wei.jtrace.api.transform.matcher;

import com.github.wei.jtrace.api.clazz.IClassDescriberTree;
import com.github.wei.jtrace.api.exception.ClassMatchException;

public interface IClassMatcher {

	boolean matchClass(IClassDescriberTree descr) throws ClassMatchException;
		
}
