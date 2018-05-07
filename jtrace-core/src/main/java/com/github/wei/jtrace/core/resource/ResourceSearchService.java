package com.github.wei.jtrace.core.resource;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.beans.BeanRef;
import com.github.wei.jtrace.api.config.IConfig;
import com.github.wei.jtrace.api.resource.IClassLoaderTree;
import com.github.wei.jtrace.api.resource.IResource;
import com.github.wei.jtrace.api.resource.IResourceSearcher;
import com.github.wei.jtrace.api.service.IAsyncService;
import com.github.wei.jtrace.core.util.ClazzUtil;

@Bean(name="resourceSearcher", type=IResourceSearcher.class)
public class ResourceSearchService implements IAsyncService, IResourceSearcher {
	static Logger log = LoggerFactory.getLogger("ResourceSearchService");
	
	@BeanRef(name="instrumentation")
	private Instrumentation instrumentation;
	
	private volatile boolean isRunning = false;
	
	private List<WeakReference<ClassLoader> > classLoaders = new ArrayList<WeakReference<ClassLoader> >();

	private List<IClassLoaderTree> classLoaderTrees = new ArrayList<IClassLoaderTree>();
	
	private ReadWriteLock classLoaderLock = new ReentrantReadWriteLock();
	
	private Object stopLock = new Object();
	
	@Override
	public String getId() {
		return "ClassLoaderCollectorService";
	}

	@Override
	public boolean start(IConfig config) {
		addClassLoader(ResourceSearchService.class.getClassLoader());
		addClassLoader(ClassLoader.getSystemClassLoader());
		addClassLoaderFromLoaded();
		
		instrumentation.addTransformer(classFileTransformer);
		isRunning = true;
		return true;
	}

	private void addClassLoaderFromLoaded() {
		Class<?>[] allclasses = instrumentation.getAllLoadedClasses();
		if(allclasses != null) {
			for(Class<?> clazz  : allclasses) {
				ClassLoader loader = clazz.getClassLoader();
				if(loader != null) {
					addClassLoader(loader);
				}
			}
		}
	}
	
	@Override
	public void stop() {
		instrumentation.removeTransformer(classFileTransformer);
		isRunning = false;
		log.debug("stop ResourceSearchService");
		
		synchronized (stopLock) {
			stopLock.notify();
		}
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}

	private void addClassLoader(ClassLoader loader){
		if(loader == null)
			return;
		
		Lock writeLock = classLoaderLock.writeLock();
		
		try{
			writeLock.lock();
		
			if(contains(loader)) {
				return;
			}
			
			log.debug("Add classloader {} for resource search", loader);
			
			classLoaders.add(new WeakReference<ClassLoader>(loader));
			
			DefaultClassLoaderTree classLoaderTree = new DefaultClassLoaderTree(loader);
			
			//设置parent
			if(loader.getParent() == null){
				classLoaderTrees.add(classLoaderTree);
			}else{
				boolean found_parent = false;
				for(int i=0;i<classLoaderTrees.size();i++){
					DefaultClassLoaderTree tree = (DefaultClassLoaderTree)classLoaderTrees.get(i);
					if(tree.addChild(classLoaderTree)){
						found_parent = true;
						break;
					}
				}
				if(!found_parent){
					classLoaderTrees.add(classLoaderTree);
				}
			}
			
			//查找子classLoader
			Iterator<IClassLoaderTree> it = classLoaderTrees.iterator();
			while(it.hasNext()){
				DefaultClassLoaderTree tree = (DefaultClassLoaderTree)it.next();
				if(classLoaderTree.addChild(tree)){
					it.remove();
				}
			}
		
		}finally {
			writeLock.unlock();
		}
	}
	
	
	
	private boolean contains(ClassLoader loader){
		//WeakReference 可能会造成幻读效果
		for(WeakReference<ClassLoader> loaderRef : classLoaders){
			if(loaderRef.get() == loader){
				return true;
			}
		}
		return false;
	}
	
	private  ClassFileTransformer classFileTransformer = new ClassFileTransformer() {
		@Override
		public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
				ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
			//AgentClassLoader、AgentClass 不进行嵌码
			
			if(ClazzUtil.isJtraceClass(loader, className)) {
				return null;
			}
			
			//BootstrapLoader 不进行嵌码
			if(loader != null){
				addClassLoader(loader);
			}
			
			return null;
		}

	};

	@Override
	public void run() {
		log.debug("running...");
		try{
			synchronized (stopLock) {
				while(isRunning()){
					log.debug("checking...");
					
					Iterator<WeakReference<ClassLoader> > it = classLoaders.iterator();
					while(it.hasNext()){
						if(it.next().get() == null){
							it.remove();
						}
					}
					
					//每分钟执行一次
					stopLock.wait(60000);
				}
			}
		}catch(Exception e){
			log.warn("running exception", e);
		}
		log.debug("stoped");
	}

	@Override
	public IResource searchResource(String path) {
		Lock lock = classLoaderLock.readLock();
		try{
			lock.lock();
			
			List<WeakReference<ClassLoader>>  tempLoaders = classLoaders;
			for(int i=0;i < tempLoaders.size(); i++){
				
				WeakReference<ClassLoader> loaderRef = tempLoaders.get(i);
				ClassLoader loader = loaderRef.get();
				if(loader != null){
					URL url = loader.getResource(path);
					if(url != null){
						return new DefaultResource(loaderRef, url);
					}
				}
			}
			
		}finally{
			lock.unlock();
		}
		return null;
	}

	@Override
	public List<ClassLoader> getClassLoaders() {
		Lock lock = classLoaderLock.readLock();
		try{
			lock.lock();
			
			List<WeakReference<ClassLoader>>  tempLoaders = classLoaders;
			List<ClassLoader> resources = new ArrayList<ClassLoader>();
	
			for(int i=0;i < tempLoaders.size(); i++){
				
				WeakReference<ClassLoader> loaderRef = tempLoaders.get(i);
				ClassLoader loader = loaderRef.get();
				if(loader != null){
					resources.add(loader);
				}
			}
			
			return resources;
		}finally{
			lock.unlock();
		}
	}
	
	@Override
	public List<IClassLoaderTree> getClassLoaderTree() {
		return Collections.unmodifiableList(classLoaderTrees);
	}

	@Override
	public List<IResource> searchResourceFromTree(String path) {
		Lock lock = classLoaderLock.readLock();
		try{
			lock.lock();
			
			List<IClassLoaderTree> trees = classLoaderTrees;
			return findResourceFromChildTree(trees, path);
		}finally{
			lock.unlock();
		}
		
	}

	private List<IResource> findResourceFromChildTree(List<IClassLoaderTree> trees, String path){
		List<IResource> resources = new ArrayList<IResource>();
		
		for(IClassLoaderTree tree : trees){
			ClassLoader loader = tree.getClassLoader();
			URL url = null;
			if(loader != null){
				url = loader.getResource(path);
			}
			if(url != null){
				resources.add(new DefaultResource(new WeakReference<ClassLoader>(loader), url));
			}else{
				List<IResource> rs = findResourceFromChildTree(tree.getChilds(), path);
				if(rs.size() > 0){
					resources.addAll(rs);
				}
			}
		}
		return resources;
	}
	
	@Override
	public List<IResource> searchResourceFromAll(String path) {
		Lock lock = classLoaderLock.readLock();
		try{
			lock.lock();
			
			List<WeakReference<ClassLoader>>  tempLoaders = classLoaders;
			
			List<IResource> resources = new ArrayList<IResource>();
			
			for(int i=0;i < tempLoaders.size(); i++){
				
				WeakReference<ClassLoader> loaderRef = tempLoaders.get(i);
				ClassLoader loader = loaderRef.get();
				if(loader != null){
					URL url = loader.getResource(path);
					resources.add(new DefaultResource(loaderRef, url));
				}
			}
			
			return resources;
		}finally{
			lock.unlock();
		}
	}
}
