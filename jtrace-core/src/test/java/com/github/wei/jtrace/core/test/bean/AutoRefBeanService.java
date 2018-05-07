package com.github.wei.jtrace.core.test.bean;

import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;

@Bean(name="autoRefService")
public class AutoRefBeanService implements ITestBeanService{

	@AutoRef
	private ITestBeanService service;
	
	@Override
	public void print() {
		System.out.println("this is AutoRefBeanService");

		service.print();
	}

}
