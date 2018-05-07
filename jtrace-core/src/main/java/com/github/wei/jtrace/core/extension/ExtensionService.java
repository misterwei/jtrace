package com.github.wei.jtrace.core.extension;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.config.IConfig;
import com.github.wei.jtrace.api.service.IAsyncService;
import com.github.wei.jtrace.core.util.AgentHelper;

@Bean(type=IExtensionService.class)
public class ExtensionService implements IAsyncService, IExtensionService{
	Logger log = LoggerFactory.getLogger("ExtensionService");
	
	private File scanPath = null;
	
	private volatile boolean running = false;
	
	private final ExtensionClassLoader extensionClassLoader = new ExtensionClassLoader(ExtensionClassLoader.class.getClassLoader());
	
	private final ConcurrentHashMap<String, IAttributeHandler> handlers = new ConcurrentHashMap<String, IAttributeHandler>(); 
	
	private final Map<File, ExtensionJarInfo> jarInfos = new HashMap<File, ExtensionJarInfo>();
	
	private long scanInterval = 10000;
	
	private ReadWriteLock lock = new ReentrantReadWriteLock();
	
	@Override
	public String getId() {
		return "extension";
	}

	@Override
	public boolean start(IConfig config) {
		this.scanInterval = config.getLong("scanInterval", 10000);
		String scanPathStr = config.getString("scanPath");
		if(scanPathStr != null){
			scanPath = new File(scanPathStr);
			if(!scanPath.exists()){
				log.error("Extension path [{}] not exists", scanPathStr);
				return false;
			}
		}else{
			File agentPath = AgentHelper.getAgentDirectory();
			scanPath = new File(agentPath, "extensions");
		}
		log.info("ExtensionService will scanning path: " + scanPath);
		running = true;
		return true;
	}

	@Override
	public void stop() {
		running = false;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public void run() {
		while(running){
			File[] files = scanPath.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					if(name.endsWith(".jar")){
						return true;
					}
					return false;
				}
			});
			
			if(files != null){
				try {
					lock.readLock().lock();
					
					Map<File, ExtensionJarInfo> jars = new HashMap<File, ExtensionJarInfo>();
					for(File file: files){
						if(jarInfos.containsKey(file)){
							continue;
						}
						log.info("Scanned jar file: {}", file);
						try {
							ExtensionJarInfo jarInfo = ExtensionJarInfo.create(file);
							jars.put(file, jarInfo);
							
							extensionClassLoader.addURL(file.toURI().toURL());
						} catch (MalformedURLException e) {
							log.warn("Can't add this jar ["+file.getAbsolutePath()+"] to classloader", e);
						} catch (IOException e) {
							log.warn("Can't resolve this jar ["+file.getAbsolutePath()+"]", e);
						}
					}
					
					for(final ExtensionJarInfo jarInfo : jars.values()){
						Map<String, String> attrs = jarInfo.getAttributes();
						Set<String> keys = attrs.keySet();
						if(log.isDebugEnabled()) {
							log.debug("ExtensionJar {} attributes:{}", jarInfo.getJarPath(), jarInfo.getAttributes());
						}
						
						for(String key: keys){
							IAttributeHandler handler = handlers.get(key);
							if(handler != null){
								handle(handler, jarInfo, key, attrs.get(key));
							}
						}
					}
					
					jarInfos.putAll(jars);
				}finally {
					lock.readLock().unlock();
				}
			}
			
			try {
				Thread.sleep(scanInterval);
			} catch (InterruptedException e) {
				log.error("ExtensionService sleep failed", e);
				break;
			}
		}
		
		running = false;
	}
	
	private void handle(IAttributeHandler handler,final ExtensionJarInfo jarInfo, String key, String value) {
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
					
					Map<String, String> attrs = jarInfo.getAttributes();
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
	
}
