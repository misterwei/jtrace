package com.github.wei.jtrace.core.test;

import org.junit.Test;

import com.github.wei.jtrace.api.beans.IBeanFactory;
import com.github.wei.jtrace.core.beans.DefaultBeanFactory;
import com.github.wei.jtrace.core.test.bean.TestBean;
import com.github.wei.jtrace.core.test.bean.TestBeanService;
import com.github.wei.jtrace.core.test.bean.AutoRefBeanService;
import com.github.wei.jtrace.core.test.bean.LazyBeanService;

import junit.framework.Assert;

public class BeanFactoryTest {
	
	@Test
	public void testAutoInject() throws Exception{
		IBeanFactory beanFactory = new DefaultBeanFactory();
		TestBean testBean = beanFactory.registBean(TestBean.class);
		testBean.setName("hello");
		
		TestBeanService service = beanFactory.registBean(TestBeanService.class);
		service.print();
		
		beanFactory.registLazyBean(LazyBeanService.class);
//		LazyBeanService lazyService = beanFactory.getBean(LazyBeanService.class);
//		lazyService.print();
		
		AutoRefBeanService autoRefService = beanFactory.registBean(AutoRefBeanService.class);
		autoRefService.print();
	}
	
	@Test
	public void testBeanNotFound(){
		IBeanFactory beanFactory = new DefaultBeanFactory();
		Assert.assertTrue(!beanFactory.containsBean("noBeanName"));
	}
}
