package com.github.wei.jtrace.api.advice;

public interface IAdviceController {
	
	void addMatcher(AdviceMatcher matcher);
	
	void removeMatcher(long id);
	
	void refresh();
	
	void restore();
}
