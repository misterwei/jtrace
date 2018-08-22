package com.github.wei.jtrace.api.advice;

public interface IAdviceListenerManager {

	IAdviceListener create(Class<?> ownClass, Object own, String methodName, String methodDescr, Object[] matcherMessage);
	
	void init(IAdviceController config);
	
}
