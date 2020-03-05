package com.github.wei.jtrace.core.transform;

import java.util.List;
import java.util.Map;

import com.github.wei.jtrace.api.clazz.ClassDescriber;

public interface IQueryMatchResult {
	
	/**
	 * 返回MethodMatcher适配的类，这个结果是最终结果
	 * @return
	 */
	Map<ClassLoader, List<ClassDescriber>> getMatchedClasses();
}
