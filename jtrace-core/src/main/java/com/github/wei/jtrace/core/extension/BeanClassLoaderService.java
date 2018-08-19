package com.github.wei.jtrace.core.extension;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.beans.IBeanFactory;
import com.github.wei.jtrace.api.beans.IBeanFactoryAware;
import com.github.wei.jtrace.api.config.IConfig;
import com.github.wei.jtrace.api.service.IService;

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
	public void handle(IExtensionContext ctx, Object value) throws Exception {
		if(value != null ) {
			if(value instanceof List) {
				List<?> beans = (List<?>)value;
				for(Object className : beans){
					try{
						Class<?> clazz = ctx.loadClass(String.valueOf(className));
						log.info("RegistBean {}", className);
						
						this.beanFactory.registBean(clazz);
					}catch(Exception e){
						log.warn("Failed to loadClass "+className, e);
					}
				}
			}else if(value instanceof String){
				try{
					Class<?> clazz = ctx.loadClass(String.valueOf(value));
					log.info("RegistBean {}", value);
					
					this.beanFactory.registBean(clazz);
				}catch(Exception e){
					log.warn("Failed to loadClass "+value, e);
				}
			}
		}
	}

	@Override
	public void setBeanFactory(IBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

}
