package com.github.wei.jtrace.core.test.service;

import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.beans.BeanRef;
import com.github.wei.jtrace.api.config.IConfig;
import com.github.wei.jtrace.api.service.IService;
import com.github.wei.jtrace.api.service.Stoppable;
import com.github.wei.jtrace.core.test.bean.TestBean;

@Bean(name = "testService")
public class TestService implements IService, Stoppable {

	@BeanRef(name="testBean")
	private TestBean testBean;
	
	@Override
	public String getId() {
		return "testService";
	}

	@Override
	public boolean start(IConfig config) {
		System.out.println("testService start " + testBean.getName());
		return true;
	}

	@Override
	public void stop() {
		System.out.println("testService stop ");
	}

}
