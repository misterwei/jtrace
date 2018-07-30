package com.github.wei.jtrace.api.advice;

public interface IAdviceManager {
	
	public void registAdviceListener(final IAdviceListenerManager listener, boolean trace) throws Exception;
	
	public void removeAdviceListener(IAdviceListenerManager listener);
}
