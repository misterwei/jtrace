package com.github.wei.jtrace.api.beans;

import com.github.wei.jtrace.api.exception.BeanProcessException;

public interface IProcessingBean {
	void afterProcessComplete() throws BeanProcessException;
}
