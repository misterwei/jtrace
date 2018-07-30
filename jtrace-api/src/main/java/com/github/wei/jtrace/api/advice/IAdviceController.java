package com.github.wei.jtrace.api.advice;

public interface IAdviceController {
	
	void addMatcher(AdviceMatcher matcher);
	
	void refresh();
	
	void restore();
}
