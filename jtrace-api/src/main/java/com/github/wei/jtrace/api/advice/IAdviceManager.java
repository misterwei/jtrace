package com.github.wei.jtrace.api.advice;

import java.util.Map;

public interface IAdviceManager {
	
	public int registAdviceListener(final IAdviceListenerManager listener) throws Exception;
	
	public IAdviceListenerManager removeAdviceListener(int id);
	
	public IAdviceListenerManager getAdviceListener(int id);

	public Map<Integer, IAdviceListenerManager> getAdviceListeners();
}
