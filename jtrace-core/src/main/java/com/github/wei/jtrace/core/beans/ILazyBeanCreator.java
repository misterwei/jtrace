package com.github.wei.jtrace.core.beans;

public interface ILazyBeanCreator<T> {
	
	public T create() throws Exception;
}
