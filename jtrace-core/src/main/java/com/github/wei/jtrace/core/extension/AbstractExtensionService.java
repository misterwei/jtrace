package com.github.wei.jtrace.core.extension;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.api.config.IConfig;
import com.github.wei.jtrace.api.service.IAsyncService;
import com.github.wei.jtrace.core.util.AgentHelper;

public abstract class AbstractExtensionService implements IAsyncService{
	Logger log = LoggerFactory.getLogger("AbstractExtensionService");
	
	private File scanPath = null;
	
	private volatile boolean running = false;
	
	protected final ConcurrentHashMap<File, ExtensionJarInfo> jarInfos = new ConcurrentHashMap<File, ExtensionJarInfo>();
	
	private long scanInterval = 10000;
	
	protected ReadWriteLock lock = new ReentrantReadWriteLock();
	
	private final String defaultScanPath;
	
	public AbstractExtensionService(String defaultScanPath) {
		this.defaultScanPath = defaultScanPath;
	}

	@Override
	public boolean start(IConfig config) {
		this.scanInterval = config.getLong("scanInterval", 10000);
		String scanPathStr = config.getString("scanPath");
		if(scanPathStr != null && scanPathStr.trim().length() > 0){
			scanPath = new File(scanPathStr);
			if(!scanPath.exists()){
				log.error("Extension path [{}] not exists", scanPathStr);
				return false;
			}
		}else{
			File agentPath = AgentHelper.getAgentDirectory();
			scanPath = new File(agentPath, defaultScanPath);
		}
		log.info("will scanning path: " + scanPath);
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
						} catch (Exception e) {
							throw new Exception("Can't resolve this jar ["+file.getAbsolutePath()+"]", e);
						}
					}
					
					handle(jars);
					
					for(final ExtensionJarInfo jarInfo : jars.values()){
						Map<String, Object> attrs = jarInfo.getAttributes();
						if(log.isDebugEnabled()) {
							log.debug("ExtensionJar {} attributes:{}", jarInfo.getJarPath(), attrs);
						}
						
						handle(jarInfo);
					}
					
					jarInfos.putAll(jars);
				}catch(Exception e){
					log.warn("handle jar info failed", e);
				}finally {
					lock.readLock().unlock();
				}
			}
			
			try {
				Thread.sleep(scanInterval);
			} catch (InterruptedException e) {
				log.error("sleep failed", e);
				break;
			}
		}
		
		running = false;
	}
	
	protected void  handle(Map<File, ExtensionJarInfo> jars) throws Exception{}
	
	protected abstract void  handle(ExtensionJarInfo jarInfo);
	
	protected void remove(ExtensionJarInfo jarInfo) {
		File f = jarInfo.getFile();
		jarInfos.remove(f);
		f.renameTo(new File(f.getParentFile(), f.getName()+".removed"));
	}
}
