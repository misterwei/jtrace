package com.github.wei.jtrace.core.test.bean;

import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.beans.BeanRef;

@Bean(name = "testBeanService")
public class TestBeanService implements ITestBeanService{
	
	@BeanRef(name="testBean")
	private TestBean bean;
	
	public void print(){
		System.out.println("this is TestBeanService");
		System.out.println("testBean.name = " + bean.getName());
	}

	
}
