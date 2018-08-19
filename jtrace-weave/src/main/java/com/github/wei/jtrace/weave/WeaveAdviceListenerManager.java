package com.github.wei.jtrace.weave;

import java.net.URLClassLoader;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.api.advice.AdviceMatcher;
import com.github.wei.jtrace.api.advice.IAdviceController;
import com.github.wei.jtrace.api.advice.IAdviceListener;
import com.github.wei.jtrace.api.advice.IAdviceListenerManager;
import com.github.wei.jtrace.api.clazz.ClassDescriber;
import com.github.wei.jtrace.api.clazz.MethodDescriber;
import com.github.wei.jtrace.api.transform.matcher.IMatchedListener;
import com.github.wei.jtrace.core.extension.ExtensionJarInfo;
import com.github.wei.jtrace.weave.api.IWeaveListener;

public class WeaveAdviceListenerManager implements IAdviceListenerManager{
	static Logger log = LoggerFactory.getLogger("WeaveAdviceListenerManager");
	
	private final List<AdviceMatcher.Builder> matchers;
	private final List<AdviceMatcher.Builder> advisors;
	
	private final AtomicInteger counter = new AtomicInteger(0);
	private final int SIZE;
	private final ConcurrentHashMap<ClassLoader, URLClassLoader> classLoaders = new ConcurrentHashMap<ClassLoader, URLClassLoader>();

	private final ExtensionJarInfo jarInfo;
	
	private IAdviceController controller;
	
	public WeaveAdviceListenerManager(List<AdviceMatcher.Builder> matchers, List<AdviceMatcher.Builder> advisors, ExtensionJarInfo jarInfo) {
		this.matchers = matchers;
		this.SIZE = matchers.size();
		
		this.advisors = advisors;
		this.jarInfo = jarInfo;
	}
	
	@Override
	public IAdviceListener create(Class<?> ownClass, Object own, String methodName, String methodDescr, Object[] matcherMessage) {
		ClassLoader targetClassLoader = ownClass.getClassLoader();
		ClassLoader loader = classLoaders.get(targetClassLoader);
		try {
			if(loader == null) {
				classLoaders.putIfAbsent(targetClassLoader, new WeaveClassLoader(targetClassLoader, WeaveAdviceListenerManager.class.getClassLoader(), jarInfo));
				loader = classLoaders.get(targetClassLoader);
			}
			
			String className = (String)matcherMessage[1];
			Class<?> clazz = loader.loadClass(className);
			
			IAdviceListener listener = (IAdviceListener)clazz.newInstance();
			if(listener instanceof IWeaveListener) {
				((IWeaveListener)listener).init(ownClass, own, methodName, methodDescr);
			}
			
			return listener;
		}catch(Exception e) {
			log.warn("create advice listener failed", e);
		}
		return null;
	}

	private void addAdvisor() {
		for(AdviceMatcher.Builder  builder : advisors) {
			this.controller.addMatcher(builder.build());
		}
		this.controller.refresh();
	}
	
	@Override
	public void init(IAdviceController config) {
		this.controller = config;
		
		if(matchers.isEmpty()) {
			log.info("{} matchers is empty, code insertion begins.", jarInfo.getName());
			addAdvisor();
			return;
		}
		
		final long batchId = System.currentTimeMillis();
		for(AdviceMatcher.Builder builder : matchers) {
			builder.withId(batchId);
			builder.withMatchedListener(new IMatchedListener() {
				private final AtomicBoolean _matched = new AtomicBoolean(false);
				@Override
				public void matched(ClassDescriber classDescriber, Set<MethodDescriber> matchedMethods) {
					if(_matched.compareAndSet(false, true) && counter.incrementAndGet() == SIZE) {
						log.info("{} adaptation is successful, and code insertion begins.", jarInfo.getName());
						
						controller.removeMatcher(batchId);
						addAdvisor();
					}
				}
			});
			
			this.controller.addMatcher(builder.build());
		}
		
		this.controller.refresh();
	}

	@Override
	public String toString() {
		return "Weave@" + hashCode() + " - " + jarInfo.getName();
	}
}
