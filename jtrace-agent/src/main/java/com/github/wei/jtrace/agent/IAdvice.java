package com.github.wei.jtrace.agent;

public interface IAdvice {
	void onBegin(Object[] args);
	
	void onReturn(Object obj);
	
	void onThrow(Throwable thr);
	
	void onInvoke(Integer lineNumber, String own, String name, String desc, boolean itf);
}
