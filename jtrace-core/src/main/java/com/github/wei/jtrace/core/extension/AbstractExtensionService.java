package com.github.wei.jtrace.core.extension;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.api.config.IConfig;
import com.github.wei.jtrace.api.service.IAsyncService;
import com.github.wei.jtrace.core.util.AgentHelperUtil;

public abstract class AbstractExtensionService implements IAsyncService{
	Logger log = LoggerFactory.getLogger("AbstractExtensionService");
	
	private File scanPath = null;
	
	private volatile boolean running = false;
	
	protected final CopyOnWriteArrayList<ExtensionJarInfo> jarInfos = new CopyOnWriteArrayList<ExtensionJarInfo>();
	protected final Set<File> jarFiles = new HashSet<File>();

	private long scanInterval = 10000;
	
	protected ReentrantLock lock = new ReentrantLock();
	
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
			File agentPath = AgentHelperUtil.getAgentDirectory();
			scanPath = new File(agentPath, defaultScanPath);
		}
		log.info("Sync extension path: " + scanPath);
		scan();

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

	protected void scan(){
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
				lock.lock();

				Set<File> tempFiles = new HashSet<File>();
				List<ExtensionJarInfo> jars = new ArrayList<ExtensionJarInfo>();
				for(File file: files){
					tempFiles.add(file);
					if(jarFiles.contains(file)){
						continue;
					}

					File removedFlag = new File(file.getAbsolutePath()+".removed");
					if(removedFlag.exists()) {
						continue;
					}

					try {
						ExtensionJarInfo jarInfo = ExtensionJarInfo.create(file);

						jars.add(jarInfo);
					} catch (Exception e) {
						throw new Exception("Can't resolve this jar ["+file.getAbsolutePath()+"]", e);
					}
				}
				//替换为最新的文件列表
				jarFiles.clear();
				jarFiles.addAll(tempFiles);

				//根据加载顺序排序
				sortByLoadOrder(jars);
				handle(jars);

				for(final ExtensionJarInfo jarInfo : jars){
					Map<String, Object> attrs = jarInfo.getAttributes();
					if(log.isDebugEnabled()) {
						log.debug("Scanned extension {} attributes:{}", jarInfo.getName(), attrs);
					}

					handle(jarInfo);
				}

				jarInfos.addAll(jars);

				//最终结果排序
				sortByLoadOrder(jarInfos);
			}catch(Exception e){
				log.warn("Failed to handle jar info", e);
			}finally {
				lock.unlock();
			}
		}
	}

	@Override
	public void run() {
		while(running){
			try {
				Thread.sleep(scanInterval);
			} catch (InterruptedException e) {
				log.error("Sleep failed", e);
				break;
			}

//			log.debug("Will scanning path: {}", scanPath);
			scan();
		}
		
		running = false;
	}

	private void sortByLoadOrder(List<ExtensionJarInfo> infos){
		Collections.sort(infos, new Comparator<ExtensionJarInfo>() {
			@Override
			public int compare(ExtensionJarInfo t0, ExtensionJarInfo t1) {
				return t0.getLoadOrder() - t1.getLoadOrder();
			}
		});
	}

	protected void  handle(List<ExtensionJarInfo> jars) throws Exception{}
	
	protected abstract void  handle(ExtensionJarInfo jarInfo);
	
	public void remove(ExtensionJarInfo jarInfo) {
		File f = jarInfo.getFile();
		File nf = new File(f.getAbsolutePath() + ".removed");

		lock.lock();
		try {

			nf.createNewFile();
			jarInfos.remove(jarInfo);
			jarFiles.remove(f);
		} catch (IOException e) {
			log.error("Failed to create " + f.getName() +" removed-flag file", e);
		}finally {
			lock.unlock();
		}
	}
}
