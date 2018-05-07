package com.github.wei.jtrace.api.beans;

import com.github.wei.jtrace.api.exception.BeanProcessException;

public interface IBeanProcessor {
	
	public static final int PRIORITY_HIGH = 1;
	public static final int PRIORITY_LOW = 0;
	
	public int priority();
	
	public <T> T process(T obj, IBeanProcessorChain chain) throws BeanProcessException;
}
