package com.github.wei.jtrace.api.transform;

import com.github.wei.jtrace.api.clazz.IClassDescriberTree;
import com.github.wei.jtrace.api.exception.ClassMatchException;
import com.github.wei.jtrace.api.transform.matcher.IClassMatcher;

public interface ITransformerMatcher extends IClassMatcher{

	ITransformer matchedTransformer(IClassDescriberTree classTree) throws ClassMatchException;
}
