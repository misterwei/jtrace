package com.github.wei.jtrace.core.advisor;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.github.wei.jtrace.core.transform.TransformService;
import com.github.wei.jtrace.core.transform.matchers.IMethodMatcher;
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
	private TransformService transformService;
	
	private final ConcurrentHashMap<IAdviceListenerManager, AdvisorTransformer> listenerAndTransformers = new ConcurrentHashMap<IAdviceListenerManager, AdvisorTransformer >();

	private final ConcurrentHashMap<String, List<IAdviceListenerManager> > listeners = new ConcurrentHashMap<String, List<IAdviceListenerManager> >();
	
	
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
	
	
	/**
	 * 移除
	 */
	public void removeAdviceListener(IAdviceListenerManager listener) {
		AdvisorTransformer transformer = listenerAndTransformers.remove(listener);
		transformService.removeTransformer(transformer);
		
		Set<String> keys = listeners.keySet();
		for(String key: keys) {
			List<IAdviceListenerManager> managers = listeners.get(key);
			if(managers != null) {
				managers.remove(listener);
				if(managers.isEmpty()) {
					listeners.remove(key);
				}
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
	 * 注册切面监听器
	 * 
	 */
	public void registAdviceListener(AdviceConfig config, final IAdviceListenerManager listener, boolean relateParent) throws Exception {
		String clazz = ClazzUtil.classNameToPath(config.getClassName());
		String methods = config.getMethods();
		
		IClassMatcher classMatcher = null;
		//是否对父类也进行适配
		if(relateParent) {
			IClassDescriberTree tree = transformService.findClassDescriberTree(clazz);
			if(tree != null) {
				classMatcher = createExtractClassMatcher(tree);
			}
		}
		if(classMatcher == null) {
			classMatcher = new BaseClassMatcher(clazz);
		}
		
		boolean newTransformer = false;
		AdvisorTransformer transformer = listenerAndTransformers.get(listener);
		if(transformer == null) {
			boolean trace = config.isInvokeTrace();
			transformer = new AdvisorTransformer(trace);
			newTransformer = true;
		}
		
		if(methods != null && !methods.isEmpty()) {
			IMethodMatcher[] methodMatchers = MatcherHelper.extractMethodMatchers(methods);
			transformer.addMatcher(classMatcher, Arrays.asList(methodMatchers));
		}
		
		if(newTransformer) {
			//注册监听器，类名方法名标识与监听器绑定
			transformer.addAdvisorMatchedListener(new IAdvisorMatchedListener() {
				@Override
				public void matched(String className, String method, String desc) {
					String sign = ClazzUtil.getSignature(className, method, desc);
					addListener(sign, listener);
				}
			});
			
			listenerAndTransformers.put(listener, transformer);
			transformService.registTransformer(transformer, true);
		}else {
			transformService.refreshTransformer(transformer);
		}
		
		
	}
	
	@Override
	public void afterProcessComplete() {
		Advisor.adviceManager = this;
	}

	
}
