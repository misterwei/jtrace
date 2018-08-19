package com.github.wei.jtrace.core.extension;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.api.beans.Bean;

@Bean(type=IExtensionService.class)
public class ExtensionService extends AbstractExtensionService implements IExtensionService{
	
	static Logger log = LoggerFactory.getLogger("ExtensionService");
	
	private final ExtensionClassLoader extensionClassLoader = new ExtensionClassLoader(ExtensionClassLoader.class.getClassLoader());
	
	private final ConcurrentHashMap<String, IAttributeHandler> handlers = new ConcurrentHashMap<String, IAttributeHandler>(); 
	
	public ExtensionService() {
		super("extensions");
	}
	
	@Override
	public String getId() {
		return "extension";
	}

	@Override
	protected void handle(Map<File, ExtensionJarInfo> jars) throws Exception{
		for(final ExtensionJarInfo jarInfo : jars.values()){
			extensionClassLoader.addURL(jarInfo.getFile().toURI().toURL());
		}
	}
	
	private void handle(IAttributeHandler handler,final ExtensionJarInfo jarInfo, String key, Object value) {
		if(log.isDebugEnabled()) {
			log.debug("ExtensionJar {} attribute:{} handler:{}", jarInfo.getJarPath(), key, handler);
		}
		try {
			handler.handle(new IExtensionContext() {
				@Override
				public Class<?> loadClass(String className) throws ClassNotFoundException{
					return extensionClassLoader.loadClass(className);
				}
				@Override
				public String getJarName() {
					return jarInfo.getJarPath(); 
				}
				@Override
				public ExtensionJarInfo getJarInfo() {
					return jarInfo;
				}
			}, value);
		} catch (Exception e) {
			log.warn("IAttributeHandler["+handler.getClass()+"] handle failed with key: " + key +" value: " + value, e);
		}
	}

	@Override
	public void registAttributeHandler(String attributeName, IAttributeHandler handler) {
		lock.writeLock().lock();
		try {
			//检查已有的Jar是否有适配的
			Collection<ExtensionJarInfo> extJarInfos = jarInfos.values();
			if(extJarInfos != null) {
				Iterator<ExtensionJarInfo> it = extJarInfos.iterator();
				while(it.hasNext()) {
					final ExtensionJarInfo jarInfo = it.next();
					
					Map<String, Object> attrs = jarInfo.getAttributes();
					Set<String> keys = attrs.keySet();
					if(log.isDebugEnabled()) {
						log.debug("ExtensionJar {} attributes:{}", jarInfo.getJarPath(), jarInfo.getAttributes());
					}
					
					for(String key: keys){
						if(key.equals(attributeName)){
							handle(handler, jarInfo, key, attrs.get(key));
							break;
						}
					}
				}
			}
			
			handlers.putIfAbsent(attributeName, handler);
		}finally {
			lock.writeLock().unlock();
		}
	}


	@Override
	protected void handle(final ExtensionJarInfo jarInfo){
		try {
			Map<String, Object> attrs = jarInfo.getAttributes();
			Set<String> keys = attrs.keySet();
			for(String key : keys) {
				IAttributeHandler handler = handlers.get(key);
				if(handler != null) {
					handler.handle(new IExtensionContext() {
						@Override
						public Class<?> loadClass(String className) throws ClassNotFoundException {
							return extensionClassLoader.loadClass(className);
						}
						
						@Override
						public String getJarName() {
							return jarInfo.getJarPath();
						}
						
						@Override
						public ExtensionJarInfo getJarInfo() {
							return jarInfo;
						}
					}, attrs.get(key));
				}
			}
			
		} catch (Exception e) {
			log.warn("handle this extension jar failed", e);
		}
		
	}
	
}
