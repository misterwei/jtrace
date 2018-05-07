package com.github.wei.jtrace.core.beans;

import com.github.wei.jtrace.api.beans.IBeanProcessor;

public class DefaultBeanProcessorChain extends AbstractBeanProcessorChain{
	
	private Class<?> matchClass;
	
	public DefaultBeanProcessorChain() {}
	
	public DefaultBeanProcessorChain(Class<?> matchType) {
		this.matchClass = matchType;
	}
	
	public boolean isMatch(Class<?> beanClass){
		if(matchClass == null){
			return true;
		}
		
		return matchClass.isAssignableFrom(beanClass);
	}
	
	public Class<?> getMatchClass() {
		return matchClass;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	public void addLastBeanProcessor(IBeanProcessor beanProcessor){
		beanProcessors.add(beanProcessor);
	}
	
	public void addFirstBeanProcessor(IBeanProcessor beanProcessor){
		beanProcessors.add(0, beanProcessor);
	}
}
