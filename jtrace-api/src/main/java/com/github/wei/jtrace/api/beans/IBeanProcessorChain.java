package com.github.wei.jtrace.api.beans;

import com.github.wei.jtrace.api.exception.BeanProcessException;

public interface IBeanProcessorChain extends Cloneable{
	public boolean isMatch(Class<?> beanClass);
	
	public <T> T doProcess(T obj) throws BeanProcessException;
	
	public Object clone() throws CloneNotSupportedException;
	
}
