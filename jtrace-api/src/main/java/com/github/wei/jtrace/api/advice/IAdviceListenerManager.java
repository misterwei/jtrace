package com.github.wei.jtrace.api.advice;

public interface IAdviceListenerManager {

	public IAdviceListener create(Class<?> ownClass, Object own, String methodName, String methodDescr);
	
}
