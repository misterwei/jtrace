package com.github.wei.jtrace.api.advice;

public interface IAdviceManager {
	
	public void registAdviceListener(AdviceConfig config, final IAdviceListenerManager listener, boolean relateParent) throws Exception;
	
	public void removeAdviceListener(IAdviceListenerManager listener);
}
