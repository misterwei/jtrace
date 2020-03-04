package com.github.wei.jtrace.core.test;

import com.github.wei.jtrace.api.beans.IBeanFactory;
import com.github.wei.jtrace.api.config.IConfigFactory;
import com.github.wei.jtrace.core.beans.DefaultBeanFactory;
import com.github.wei.jtrace.core.clazz.ClassDetailCommand;
import com.github.wei.jtrace.core.clazz.ClassFinderManager;
import com.github.wei.jtrace.core.command.ClassLoaderTreeCommand;
import com.github.wei.jtrace.core.command.CommandExecutorService;
import com.github.wei.jtrace.core.config.DefaultConfigFactory;
import com.github.wei.jtrace.core.extension.BeanClassLoaderService;
import com.github.wei.jtrace.core.extension.ExtensionService;
import com.github.wei.jtrace.core.logger.LoggerConfiger;
import com.github.wei.jtrace.core.logger.RootLogger;
import com.github.wei.jtrace.core.resource.ResourceSearchService;
import com.github.wei.jtrace.core.resource.SearchResourceCommand;
import com.github.wei.jtrace.core.service.ServiceManager;
import com.github.wei.jtrace.core.transform.MatchAndRestoreService;
import com.github.wei.jtrace.core.transform.TransformService;
import com.github.wei.jtrace.core.transform.command.MatchClassCommand;
import com.github.wei.jtrace.core.transform.command.QueryMatchResultCommand;
import com.github.wei.jtrace.core.transform.command.RestoreClassCommand;
import com.github.wei.jtrace.core.util.AgentHelper;
import org.junit.Before;
import org.slf4j.Logger;

public class BaseTest {
	protected IBeanFactory beanFactory = new DefaultBeanFactory();
	
	@Before
	public void init() throws Exception{
		final IConfigFactory configFactory = new DefaultConfigFactory("jtrace.properties");
		LoggerConfiger.autoConfig(configFactory);
		
		Logger logger = RootLogger.get();
		
		logger.info("jtrace agent path: {}", AgentHelper.getAgentDirectory());
		
		beanFactory.registObject("instrumentation", new TestInstrumentation());
		beanFactory.registObject("configFactory", configFactory);
		beanFactory.registBean(ServiceManager.class);
		
		initBeanFactory(beanFactory);
	}
	
	protected void initBeanFactory(IBeanFactory beanFactory) throws Exception{
		beanFactory.registBean(CommandExecutorService.class);
		beanFactory.registBean(ResourceSearchService.class);
		beanFactory.registBean(ClassFinderManager.class);
		beanFactory.registBean(TransformService.class);
		beanFactory.registBean(MatchAndRestoreService.class);
		
		//class and classloader
		beanFactory.registBean(SearchResourceCommand.class);
		beanFactory.registBean(ClassDetailCommand.class);
		beanFactory.registBean(ClassLoaderTreeCommand.class);
		
		//match and weave
		beanFactory.registBean(MatchClassCommand.class);
		beanFactory.registBean(RestoreClassCommand.class);
		beanFactory.registBean(QueryMatchResultCommand.class);

		//扩展服务
		beanFactory.registBean(ExtensionService.class);
		beanFactory.registBean(BeanClassLoaderService.class);
	}
}
