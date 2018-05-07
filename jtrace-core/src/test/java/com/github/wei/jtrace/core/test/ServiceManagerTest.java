package com.github.wei.jtrace.core.test;

import org.junit.Test;

import com.github.wei.jtrace.api.resource.IResource;
import com.github.wei.jtrace.api.resource.IResourceSearcher;
import com.github.wei.jtrace.core.resource.ResourceSearchService;
import com.github.wei.jtrace.core.test.bean.TestBean;
import com.github.wei.jtrace.core.test.service.TestService;

public class ServiceManagerTest extends BaseTest{
	
	@Test
	public void testRegistAndStart() throws Exception{
		beanFactory.registBean(TestBean.class);
		beanFactory.registBean(TestService.class);
		beanFactory.destroyBean(TestService.class);
	}
	
	@Test
	public void testAsyncService() throws Exception{
		IResourceSearcher resourceSearch = beanFactory.getBean(IResourceSearcher.class);
		IResource resources = resourceSearch.searchResource("com/github/wei");
		System.out.println(resources);
		
		Thread.sleep(1000);
		beanFactory.destroyBean(ResourceSearchService.class);
		
		Thread.sleep(1000);
	}
}
