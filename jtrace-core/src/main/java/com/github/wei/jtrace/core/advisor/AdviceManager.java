package com.github.wei.jtrace.core.advisor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.agent.IAdvice;
import com.github.wei.jtrace.api.advice.AdviceConfig;
import com.github.wei.jtrace.api.advice.IAdviceListener;
import com.github.wei.jtrace.api.advice.IAdviceListenerManager;
import com.github.wei.jtrace.api.advice.IAdviceManager;
import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.beans.IProcessingBean;
import com.github.wei.jtrace.api.clazz.IClassDescriberTree;
import com.github.wei.jtrace.api.matcher.BaseClassMatcher;
import com.github.wei.jtrace.api.matcher.ExtractClassMatcher;
import com.github.wei.jtrace.api.matcher.IClassMatcher;
import com.github.wei.jtrace.core.matchers.IMethodMatcher;
import com.github.wei.jtrace.core.matchers.MatchAndTransformService;
import com.github.wei.jtrace.core.util.ClazzUtil;
import com.github.wei.jtrace.core.util.MatcherHelper;

/**
 * 切面管理，事件发生后通知相应的Listener
 * @author wei-8
 */
@Bean(type=IAdviceManager.class)
public class AdviceManager implements IProcessingBean, IAdviceManager{
	
	static Logger log = LoggerFactory.getLogger("AdviceManager");
	
	@AutoRef
	private MatchAndTransformService transformService;
	
	
	private final ConcurrentHashMap<IAdviceListenerManager, List<Integer> > listenerAndTransformers = new ConcurrentHashMap<IAdviceListenerManager, List<Integer> >();

	private final ConcurrentHashMap<String, List<IAdviceListenerManager> > listeners = new ConcurrentHashMap<String, List<IAdviceListenerManager> >();
	
	private final ConcurrentHashMap<String, Integer> transformerCache = new ConcurrentHashMap<String, Integer>();
	
	/**
	 * 方法开始，创建切面
	 * @param ownClass
	 * @param own
	 * @param methodName
	 * @param methodDescr
	 * @param args
	 */
	public IAdvice createAdvice(Class<?> ownClass, Object own, String methodName, String methodDescr) {
		String sign = ClazzUtil.getSignature(ClazzUtil.classNameToPath(ownClass.getName()), methodName, methodDescr);
		List<IAdviceListener> advices = new  ArrayList<IAdviceListener>();
		
		List<IAdviceListenerManager> holders = listeners.get(sign);
		
		if(holders != null) {
			for(IAdviceListenerManager manager : holders) {
				IAdviceListener listener = manager.create(ownClass, own, methodName, methodDescr);
				if(listener != null) {
					advices.add(listener);
				}
			}
		}
		return new AdviceInvoker(advices);
	}

	
	private List<IAdviceListenerManager> getAdviceListeners(String key){
		List<IAdviceListenerManager> values = listeners.get(key);
		if(values == null) {
			values = listeners.putIfAbsent(key, new CopyOnWriteArrayList<IAdviceListenerManager>());
			if(values == null) { //第一次返回NULL
				values = listeners.get(key);
			}
		}
		return values;
	}
	
	private void addListener(String key, IAdviceListenerManager manager) {
		List<IAdviceListenerManager> values = getAdviceListeners(key);
		if(!values.contains(manager)) {
			values.add(manager);
		}
	}
	
	private void saveListenerAndTransformerId(IAdviceListenerManager listener, int transformerId) {
		List<Integer> ids = listenerAndTransformers.get(listener);
		if(ids == null) {
			ids = listenerAndTransformers.putIfAbsent(listener, new CopyOnWriteArrayList<Integer>());
			if(ids == null) { //第一次返回NULL
				ids = listenerAndTransformers.get(listener);
			}
		}
		
		if(!ids.contains(transformerId)) {
			ids.add(transformerId);
		}
	}
	
	private AdvisorTransformer createTransformer(IClassMatcher classMatcher,boolean trace) throws Exception{
		AdvisorTransformer transformer =  new AdvisorTransformer(classMatcher, trace);
		return transformer;
	}
	
	/**
	 * 移除
	 */
	public void removeAdviceListener(IAdviceListenerManager listener) {
		Set<String> keys = listeners.keySet();
		for(String key: keys) {
			List<IAdviceListenerManager> managers = listeners.get(key);
			if(managers != null) {
				managers.remove(listener);
			}
		}
		
		//后面需要优化
		List<Integer> ids = listenerAndTransformers.remove(listener);
		if(ids != null) {
			List<String> removeKeys = new ArrayList<String>();
			for(Integer id : ids) {
				transformService.removeTransformerById(id);
				Set<String> clazzKeys = transformerCache.keySet();
				for(String key: clazzKeys) {
					if(id.equals(transformerCache.get(key))) {
						removeKeys.add(key);
					}
				}
			}
			for(String key : removeKeys) {
				transformerCache.remove(key);
			}
		}
	}
	
	private IClassMatcher createExtractClassMatcher(IClassDescriberTree tree) {
		List<String> classNames = new ArrayList<String>();
		classNames.add(tree.getClassDescriber().getName());
		IClassDescriberTree t = tree.getSuperClass();
		while(t != null && t.getSuperClass() != null) {
			classNames.add(t.getClassDescriber().getName());
			t = t.getSuperClass();
		}
		return new ExtractClassMatcher(classNames.toArray(new String[classNames.size()]));
	}
	
	/**
	 * 注册切面监听器。
	 * @param listener
	 * @param config
	 */
	public void registAdviceListener(AdviceConfig config, final IAdviceListenerManager listener, boolean relateParent) throws Exception {
		String clazz = config.getClassName().replace('.', '/');
		String methods = config.getMethods();
		
		AdvisorTransformer transformer = null;
		Integer transformerId = transformerCache.get(clazz);
		if(transformerId != null) {
			transformer = (AdvisorTransformer)transformService.getTransformer(transformerId);
			if(transformer != null) {
				if(methods != null && !methods.isEmpty()) {
					IMethodMatcher[] methodMatchers = MatcherHelper.extractMethodMatchers(methods);
					transformer.addMethodMatcher(methodMatchers);
				}
				transformService.refreshTransformer(transformerId);
				return;
			}
		}
		
		boolean trace = config.isInvokeTrace();
		
		IClassMatcher classMatcher = null;
		if(relateParent) {
			IClassDescriberTree tree = transformService.findClassDescriberTree(clazz);
			if(tree != null) {
				classMatcher = createExtractClassMatcher(tree);
			}
		}
		if(classMatcher == null) {
			classMatcher = new BaseClassMatcher(clazz);
		}
		
		transformer = createTransformer(classMatcher, trace);
		if(methods != null && !methods.isEmpty()) {
			IMethodMatcher[] methodMatchers = MatcherHelper.extractMethodMatchers(methods);
			transformer.addMethodMatcher(methodMatchers);
		}
		
		transformerId = transformService.registTransformer(transformer, true);
		
		transformerCache.put(clazz, transformerId);
		saveListenerAndTransformerId(listener, transformerId);
		
		//如果遇到并发，正好适配类加载，那么第一次可能执行不到AdviceListener
		transformer.addAdvisorMatchedListener(new IAdvisorMatchedListener() {
			@Override
			public void matched(String className, String method, String desc) {
				String sign = ClazzUtil.getSignature(className, method, desc);
				addListener(sign, listener);
			}
		});
		
	}
	
	@Override
	public void afterProcessComplete() {
		Advisor.adviceManager = this;
	}

	
}
