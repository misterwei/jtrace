package com.github.wei.jtrace.core.beans;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.wei.jtrace.api.beans.IBeanProcessor;
import com.github.wei.jtrace.api.beans.IBeanProcessorChain;
import com.github.wei.jtrace.api.exception.BeanProcessException;

public abstract class AbstractBeanProcessorChain implements IBeanProcessorChain {
	protected List<IBeanProcessor > beanProcessors = new CopyOnWriteArrayList<IBeanProcessor >();
	private int index = -1;
	
	@Override
	public abstract boolean isMatch(Class<?> beanClass);

	@Override
	public <T> T doProcess(T obj) throws BeanProcessException {
		index++;
		
		if(index < beanProcessors.size()){
			return beanProcessors.get(index).process(obj, this);
		}
		return obj;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
