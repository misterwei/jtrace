package com.github.wei.jtrace.core;

import com.github.wei.jtrace.api.beans.IBeanFactory;
import com.github.wei.jtrace.api.config.IConfigFactory;
import com.github.wei.jtrace.api.exception.BeanCheckException;
import com.github.wei.jtrace.api.exception.BeanInstantiationException;
import com.github.wei.jtrace.api.exception.BeanProcessException;
import com.github.wei.jtrace.core.beans.DefaultBeanFactory;
import com.github.wei.jtrace.core.clazz.ClassDetailCommand;
import com.github.wei.jtrace.core.clazz.ClassFinderManager;
import com.github.wei.jtrace.core.command.ClassLoaderTreeCommand;
import com.github.wei.jtrace.core.command.CommandExecutorService;
import com.github.wei.jtrace.core.config.DefaultConfigFactory;
import com.github.wei.jtrace.core.export.ExportClassService;
import com.github.wei.jtrace.core.extension.BeanClassLoaderService;
import com.github.wei.jtrace.core.extension.ExtensionService;
import com.github.wei.jtrace.core.logger.LoggerConfiger;
import com.github.wei.jtrace.core.logger.RootLogger;
import com.github.wei.jtrace.core.resource.ResourceSearchService;
import com.github.wei.jtrace.core.resource.SearchResourceCommand;
import com.github.wei.jtrace.core.service.ServiceManager;
import com.github.wei.jtrace.core.transform.MatchAndRestoreService;
import com.github.wei.jtrace.core.transform.TransformService;
import com.github.wei.jtrace.core.transform.command.RemoveTransformerCommand;
import com.github.wei.jtrace.core.transform.command.RestoreClassCommand;
import com.github.wei.jtrace.core.transform.command.ShowAllTransformerCommand;
import com.github.wei.jtrace.core.util.AgentHelperUtil;
import org.slf4j.Logger;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class JtraceLauncher {
	public static final ConcurrentHashMap<String, ClassLoader> EXPORT_CLASS = new ConcurrentHashMap<String, ClassLoader>();

	public void start(final String[] args, final Instrumentation inst) throws Exception{
		
		URL properties = AgentHelperUtil.getAgentPropertiesFile();
		System.out.println("jtrace properties file " + properties);
		
		IConfigFactory configFactory = new DefaultConfigFactory(properties);
		
		LoggerConfiger.autoConfig(configFactory);
		
		final Logger logger = RootLogger.get();
		
		File agentDir = AgentHelperUtil.getAgentDirectory();
		logger.info("jtrace agent path: {}", agentDir);
		
		
		final IBeanFactory beanFactory = new DefaultBeanFactory();
		beanFactory.registObject("instrumentation", inst);
		beanFactory.registObject("configFactory", configFactory);
		beanFactory.registBean(ServiceManager.class);
		
		
		initBeanFactory(beanFactory);
		
		Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run() {
				try {
					beanFactory.destroyAll();
				} catch (BeanProcessException e) {
					logger.error("BeanFactory destroy all beans failed", e);
				}
				LoggerConfiger.clear();
			}
		});
	}
	
	private void initBeanFactory(IBeanFactory beanFactory) throws BeanInstantiationException, BeanProcessException, BeanCheckException{
		beanFactory.registBean(AgentHelperImpl.class);
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
		beanFactory.registBean(RestoreClassCommand.class);
		beanFactory.registBean(ShowAllTransformerCommand.class);
		beanFactory.registBean(RemoveTransformerCommand.class);

		beanFactory.registBean(ExportClassService.class);

		//扩展服务
		beanFactory.registBean(ExtensionService.class);
		beanFactory.registBean(BeanClassLoaderService.class);

	}
	
}
