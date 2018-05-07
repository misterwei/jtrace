package com.github.wei.jtrace.core.test.bean;

import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.beans.BeanRef;
import com.github.wei.jtrace.api.beans.IBeanFactory;
import com.github.wei.jtrace.api.beans.IBeanFactoryAware;

@Bean(name = "lazyBeanService", type=ITestBeanService.class)
public class LazyBeanService implements IBeanFactoryAware, ITestBeanService{
	
	@BeanRef(name="testBean")
	private TestBean bean;

	public void print(){
		System.out.println("this is LazyBeanService");
		System.out.println("testBean.name = " + bean.getName());
	}

	@Override
	public void setBeanFactory(IBeanFactory beanFactory) {
		System.out.println("lazyBeanService setBeanFactory");
	}
	
}
