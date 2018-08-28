package com.github.wei.jtrace.api.advice;

public interface IAdviceController {
	
	void addMatcher(AdviceMatcher matcher);
	
	void removeMatcher(long matcherId);
	
	void refresh(long matcherId);
	
	void refresh();
	
	void restore();
}
