package com.github.wei.jtrace.core.clazz;

import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.clazz.ClassDescriber;
import com.github.wei.jtrace.api.clazz.IClassFinder;
import com.github.wei.jtrace.api.clazz.IClassFinderManager;
import com.github.wei.jtrace.api.exception.ClassFinderException;
import com.github.wei.jtrace.api.resource.IResource;
import com.github.wei.jtrace.api.resource.IResourceSearcher;
import com.github.wei.jtrace.core.util.ClazzUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Bean(type=IClassFinderManager.class)
public class ClassFinderManager implements IClassFinderManager {
	
	@AutoRef
	private IResourceSearcher searcher;
	
	private ClassFinder defaultClassFinder = new ClassFinder();
	
	private LoadingCache<ClassLoader, IClassFinder> classFinderCache = CacheBuilder.newBuilder()
			.expireAfterAccess(30, TimeUnit.SECONDS)
			.build(new CacheLoader<ClassLoader, IClassFinder>(){

				@Override
				public IClassFinder load(ClassLoader loader) throws Exception {
					return new ClassFinder(loader);
				}
				
			});
	
	private Cache<String, ClassDescriber> classCache = CacheBuilder.newBuilder()
			.expireAfterAccess(60, TimeUnit.SECONDS)
			.build();
	
	@Override
	public IClassFinder getClassFinder(ClassLoader loader) throws ClassFinderException {
		try {
			if(loader == null) {
				return defaultClassFinder;
			}
			
			return classFinderCache.get(loader);
		} catch (ExecutionException e) {
			throw new ClassFinderException(e);
		}
	}

	private class ClassFinder implements IClassFinder{
		private ClassLoader loader;
		
		public ClassFinder() {
			
		}
		
		public ClassFinder(ClassLoader loader) {
			this.loader = loader;
		}
		
		@Override
		public ClassDescriber find(final String className) throws ClassFinderException {
			
			try {
				
				ClassDescriber result = classCache.get(className, new Callable<ClassDescriber>() {

					@Override
					public ClassDescriber call() throws Exception {
						ClassDescriber descr = null;
						String classPath = className + ".class";
						if(loader != null) {
							URL url = loader.getResource(classPath);
							if(url != null) {
								descr = ClazzUtil.extractClassDescriber(url);
							}
						}
						
						if(descr == null) {
							IResource resource = searcher.searchResource(classPath);
							if(resource != null && resource.getURL() != null) {
								descr = ClazzUtil.extractClassDescriber(resource.getURL());
							}
						}
						
						if(descr == null) {
							throw new ClassFinderException("class "+className+" not found");
						}
						
						return descr;
					}
					
				});
				
				return result;
			} catch (Exception e) {
				throw new ClassFinderException(e);
			}
			
		}

	}
	
}
