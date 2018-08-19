package com.github.wei.jtrace.server.test;

import org.junit.Test;

import com.github.wei.jtrace.api.beans.IBeanFactory;
import com.github.wei.jtrace.core.test.BaseTest;
import com.github.wei.jtrace.server.NettyRestServer;

public class NettyRestServerTest extends BaseTest{

	@Override
	protected void initBeanFactory(IBeanFactory beanFactory) throws Exception{
		super.initBeanFactory(beanFactory);
		beanFactory.registBean(NettyRestServer.class);
	}
	
	@Test
	public void testRun() throws Exception{
		Thread.sleep(3000);
	}
}
