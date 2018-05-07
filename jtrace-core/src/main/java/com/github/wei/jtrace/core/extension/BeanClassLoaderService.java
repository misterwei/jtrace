package com.github.wei.jtrace.core.extension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.beans.IBeanFactory;
import com.github.wei.jtrace.api.beans.IBeanFactoryAware;
import com.github.wei.jtrace.api.config.IConfig;
import com.github.wei.jtrace.api.service.IService;
import com.google.common.base.Splitter;

@Bean
public class BeanClassLoaderService implements IService, IAttributeHandler, IBeanFactoryAware{
	
	Logger log = LoggerFactory.getLogger("BeanClassLoaderService");
	
	@AutoRef
	private IExtensionService extensionService;
	
	private IBeanFactory beanFactory;
	
	@Override
	public String getId() {
		return "BeanClassLoaderService";
	}

	@Override
	public boolean start(IConfig config) {
		extensionService.registAttributeHandler("Bean-Classes", this);
		return true;
	}

	@Override
	public void handle(IExtensionContext ctx, String value) throws Exception {
		Iterable<String> values = Splitter.on(',').omitEmptyStrings().trimResults().split(value);
		for(String className : values){
			try{
				Class<?> clazz = ctx.loadClass(className);
				log.info("RegistBean {}", className);
				
				this.beanFactory.registBean(clazz);
			}catch(Exception e){
				log.warn("Failed to loadClass "+className, e);
			}
		}
	}

	@Override
	public void setBeanFactory(IBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

}
