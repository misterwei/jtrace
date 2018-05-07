package com.github.wei.jtrace.core.beans;

import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.beans.IBeanFactory;

public class AutoInjectProcessorChain extends AbstractBeanProcessorChain{
	
	public AutoInjectProcessorChain(IBeanFactory beanFactory) {
		beanProcessors.add(new BeanRefInjectProcessor(beanFactory));
		beanProcessors.add(new AutoRefInjectProcessor(beanFactory));
	}
	
	public boolean isMatch(Class<?> beanClass){
		return beanClass.getAnnotation(Bean.class) != null;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
}
