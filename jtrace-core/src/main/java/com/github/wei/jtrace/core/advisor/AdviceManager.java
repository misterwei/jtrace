package com.github.wei.jtrace.core.advisor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.agent.IAdvice;
import com.github.wei.jtrace.api.advice.AdviceMatcher;
import com.github.wei.jtrace.api.advice.AdviceMatcher.MatchType;
import com.github.wei.jtrace.api.advice.IAdviceController;
import com.github.wei.jtrace.api.advice.IAdviceListener;
import com.github.wei.jtrace.api.advice.IAdviceListenerManager;
import com.github.wei.jtrace.api.advice.IAdviceManager;
import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.beans.IProcessingBean;
import com.github.wei.jtrace.api.clazz.IClassDescriberTree;
import com.github.wei.jtrace.api.transform.matcher.IClassMatcher;
import com.github.wei.jtrace.api.transform.matcher.IMethodMatcher;
import com.github.wei.jtrace.core.transform.IMatchedListener;
import com.github.wei.jtrace.core.transform.TransformService;
import com.github.wei.jtrace.core.transform.matchers.BaseClassMatcher;
import com.github.wei.jtrace.core.transform.matchers.ExtractClassMatcher;
import com.github.wei.jtrace.core.transform.matchers.InterfaceClassMatcher;
import com.github.wei.jtrace.core.transform.matchers.OrClassMatcher;
import com.github.wei.jtrace.core.util.ClazzUtil;
import com.github.wei.jtrace.core.util.Constants;
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
		for(Iterator<String> it = keys.iterator(); it.hasNext();) {
			List<IAdviceListenerManager> managers = listeners.get(it.next());
			if(managers != null) {
				managers.remove(listener);
				if(managers.isEmpty()) {
					it.remove();
				}
			}
		}
	}
	
	private IClassMatcher createExtractClassMatcher(IClassDescriberTree tree) {
		if(tree == null) {
			return null;
		}
		
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
	public void registAdviceListener(final IAdviceListenerManager listener, boolean trace) throws Exception {
		
		if(listenerAndTransformers.containsKey(listener)) {
			return;
		}
		
		final AdvisorTransformer transformer = new AdvisorTransformer(trace);
		
		transformer.addAdvisorMatchedListener(new IMatchedListener() {
			@Override
			public void matched(String className, String method, String desc) {
				String sign = ClazzUtil.getSignature(className, method, desc);
				addListener(sign, listener);
			}
		});
		
		listenerAndTransformers.put(listener, transformer);
		
		transformService.registTransformer(transformer, false);
		
		listener.init(new IAdviceController() {
			@Override
			public void addMatcher(AdviceMatcher matcher) {
				String className = matcher.getClassName().replace('.', '/');
				List<String> methods = matcher.getMethods();
				
				IClassMatcher classMatcher = null;
				IClassMatcher relateParentMatcher = null;
				//是否对父类也进行适配
				if(matcher.isRelateParent()) {
					IClassDescriberTree tree = transformService.findClassDescriberTree(className);
					if(tree != null ) {
						IClassDescriberTree superTree = tree.getSuperClass();
						if(superTree != null && !Constants.CLASS_OBJECT.equals(superTree.getClassDescriber().getName())) {
							relateParentMatcher = createExtractClassMatcher(superTree);
						}
					}
				}
				
				//对当前类进行适配
				if(matcher.getMatchType() == MatchType.BASE) {
					classMatcher = new BaseClassMatcher(className);
				}else if(matcher.getMatchType() == MatchType.INTERFACE) {
					classMatcher = new InterfaceClassMatcher(className);
				}else {
					classMatcher = new ExtractClassMatcher(className);
				}
				
				if(relateParentMatcher != null) {
					classMatcher = new OrClassMatcher(classMatcher, relateParentMatcher);
				}
				
				List<IMethodMatcher> methodMatchers = null;
				if(methods != null && !methods.isEmpty()) {
					methodMatchers = MatcherHelper.extractMethodMatchers(methods);
				}
				transformer.addMatcher(classMatcher, methodMatchers);
			}
			
			@Override
			public void refresh() {
				transformService.refreshTransformer(transformer);
			}
			
			@Override
			public void restore() {
				transformService.removeTransformer(transformer);
			}
		});
	}
	
	@Override
	public void afterProcessComplete() {
		Advisor.adviceManager = this;
	}

	
}
