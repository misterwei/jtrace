package com.github.wei.jtrace.core.extension;

import com.github.wei.jtrace.api.beans.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Bean(type=IExtensionService.class)
public class ExtensionService extends AbstractExtensionService implements IExtensionService {
	
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
	protected void handle(List< ExtensionJarInfo> jars) throws Exception{
		for(final ExtensionJarInfo jarInfo : jars){
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
	public void registerAttributeHandler(String attributeName, IAttributeHandler handler) {
		lock.lock();
		try {
			//检查已有的Jar是否有适配的
			Collection<ExtensionJarInfo> extJarInfos = jarInfos;
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
			lock.unlock();
		}
	}


	@Override
	protected void handle(final ExtensionJarInfo jarInfo){
		try {
			Map<String, Object> attrs = jarInfo.getAttributes();
			Set<Map.Entry<String, Object>> entries = attrs.entrySet();
			for(Map.Entry<String, Object> entry : entries) {
				IAttributeHandler handler = handlers.get(entry.getKey());
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
					}, entry.getValue());
				}
			}
			
		} catch (Exception e) {
			log.warn("Handle this extension jar " + jarInfo + " failed", e);
		}
		
	}
}
