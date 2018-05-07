package com.github.wei.jtrace.core.beans;

import com.github.wei.jtrace.api.beans.IBeanFactory;
import com.github.wei.jtrace.api.beans.IBeanFactoryAware;
import com.github.wei.jtrace.api.beans.IBeanPostProcessor;
import com.github.wei.jtrace.api.beans.IBeanProcessorChain;
import com.github.wei.jtrace.api.exception.BeanProcessException;

public class BeanFactoryInjectProcessor implements IBeanPostProcessor{
	private IBeanFactory beanFactory;
	
	public BeanFactoryInjectProcessor(IBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
	
	@Override
	public int priority() {
		return 1;
	}

	@Override
	public <T> T process(T obj, IBeanProcessorChain chain) throws BeanProcessException {
		if(obj instanceof IBeanFactoryAware){
			IBeanFactoryAware aware = (IBeanFactoryAware)obj;
			aware.setBeanFactory(beanFactory);
		}
		return chain.doProcess(obj);
	}

}
