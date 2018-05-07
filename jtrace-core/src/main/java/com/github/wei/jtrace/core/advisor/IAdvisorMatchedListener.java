package com.github.wei.jtrace.core.advisor;

public interface IAdvisorMatchedListener {
	
	void matched(String className, String method, String desc);
	
}
