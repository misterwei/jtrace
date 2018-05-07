package com.github.wei.jtrace.core.test.bean;

import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.beans.IProcessingBean;

@Bean(name = "testBean")
public class TestBean implements IProcessingBean{
	
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void afterProcessComplete() {
		this.name = "afterProcessCompleted";
	}
	
}
