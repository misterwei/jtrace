package com.github.wei.jtrace.core.advisor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.agent.IAdvice;
import com.github.wei.jtrace.api.advice.AdviceMatcher;
import com.github.wei.jtrace.api.advice.AdviceMatcher.MatchType;
import com.github.wei.jtrace.api.advice.AdviceMatcher.MethodAdviceMatcher;
import com.github.wei.jtrace.api.advice.IAdviceController;
import com.github.wei.jtrace.api.advice.IAdviceListener;
import com.github.wei.jtrace.api.advice.IAdviceListenerManager;
import com.github.wei.jtrace.api.advice.IAdviceListenerManagerRemoved;
import com.github.wei.jtrace.api.advice.IAdviceManager;
import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.beans.IProcessingBean;
import com.github.wei.jtrace.api.clazz.IClassDescriberTree;
import com.github.wei.jtrace.api.transform.matcher.IClassMatcher;
import com.github.wei.jtrace.api.transform.matcher.IMatchedListener;
import com.github.wei.jtrace.api.transform.matcher.IMethodMatcher;
import com.github.wei.jtrace.api.transform.matcher.IMethodMatcherWithContext;
import com.github.wei.jtrace.api.transform.matcher.MatcherContext;
import com.github.wei.jtrace.core.transform.TransformService;
import com.github.wei.jtrace.core.transform.matchers.BaseClassMatcher;
import com.github.wei.jtrace.core.transform.matchers.ExtractClassMatcher;
import com.github.wei.jtrace.core.transform.matchers.InterfaceClassMatcher;
import com.github.wei.jtrace.core.transform.matchers.Matcher;
import com.github.wei.jtrace.core.transform.matchers.MethodAndMatcher;
import com.github.wei.jtrace.core.transform.matchers.MethodAnnotationMatcher;
import com.github.wei.jtrace.core.transform.matchers.MethodMatcherWithContext;
import com.github.wei.jtrace.core.transform.matchers.MethodOrMatcher;
import com.github.wei.jtrace.core.transform.matchers.OrClassMatcher;
import com.github.wei.jtrace.core.util.Constants;
import com.github.wei.jtrace.core.util.IdGenerator;
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
	
	private final AdvisorTransformer transformer = new AdvisorTransformer();
	
	private final ConcurrentHashMap<Integer, IAdviceListenerManager > listeners = new ConcurrentHashMap<Integer, IAdviceListenerManager >();
	
	private IdGenerator idg = IdGenerator.generate(Integer.MAX_VALUE);
	
	/**
	 * 方法开始，创建切面
	 * @param ownClass
	 * @param own
	 * @param methodName
	 * @param methodDescr
	 * @param args
	 */
	public IAdvice createAdvice(Class<?> ownClass, Object own, String methodName, String methodDescr, Object[] matcherMessages) {
		List<IAdviceListener> advices = new  ArrayList<IAdviceListener>();
		
		for(Object message : matcherMessages) {
			Object[] args = (Object[])message;
			
			IAdviceListenerManager manager = listeners.get(args[0]);
			if(manager != null) {
				IAdviceListener listener = manager.create(ownClass, own, methodName, methodDescr, args);
				if(listener != null) {
					advices.add(listener);
				}
			}
		}
		
		return new AdviceInvoker(advices);
	}
	
	/**
	 * 移除
	 */
	public IAdviceListenerManager removeAdviceListener(int id) {
		IClassMatcher matcher = transformer.removeGroupClassMatcherById(id);
		if(matcher != null) {
			transformService.refreshTransformer(matcher);
		}
		
		IAdviceListenerManager manager = listeners.remove(id);
		if(manager instanceof IAdviceListenerManagerRemoved) {
			((IAdviceListenerManagerRemoved)manager).onRemoved();
		}
		return manager;
	}
	
	@Override
	public IAdviceListenerManager getAdviceListener(int id) {
		return listeners.get(id);
	}
	
	@Override
	public Map<Integer, IAdviceListenerManager> getAdviceListeners() {
		return Collections.unmodifiableMap(listeners);
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
	public int registAdviceListener(final IAdviceListenerManager listener) throws Exception {
		
		final int managerId = idg.next();
		
		listeners.putIfAbsent(managerId, listener);
		
		listener.init(new IAdviceController() {
			@Override
			public void addMatcher(AdviceMatcher matcher) {
				String className = matcher.getMatchClassName().replace('.', '/');
				List<MethodAdviceMatcher> methods = matcher.getMatchMethods();
				
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
				
				List<IMethodMatcherWithContext> methodMatchers = null;
				if(methods != null && !methods.isEmpty()) {
					methodMatchers = new ArrayList<IMethodMatcherWithContext>(methods.size());
					for(MethodAdviceMatcher mm : methods) {
						String ann = mm.getAnnotation();
						String name = mm.getName();
						
						if(ann == null && name ==null) {
							continue;
						}
						
						IMethodMatcher methodMatcher = null;
						if(name != null && !"".equals(name)) {
							List<IMethodMatcher> ms = MatcherHelper.extractMethodMatchers(name);
							if(ms != null && !ms.isEmpty()) {
								methodMatcher = new MethodOrMatcher(ms.toArray(new IMethodMatcher[ms.size()]));
							}
						}
						if(ann != null  && !"".equals(ann)) {
							if(methodMatcher == null) {
								methodMatcher = new MethodAnnotationMatcher(ann);
							}else {
								methodMatcher = new MethodAndMatcher(methodMatcher, new MethodAnnotationMatcher(ann));
							}
						}
						methodMatchers.add(new MethodMatcherWithContext(methodMatcher, mm.getParameters()));
					}
					
				}
				
				//传递groupId、message，用于写入嵌码类。
				MatcherContext context = matcher.getContext();
				context.addForList(Constants.MATCHER_CONTEXT_MATCHER_MESSAGES, new Object[] {managerId, matcher.getMessage()});
				
				IMatchedListener matcheListener = matcher.getMatchedListener();
				if(matcheListener != null) {
					transformer.addMatcher(managerId, new Matcher(matcher.getId(), context, classMatcher, methodMatchers, matcheListener));
				}else {
					transformer.addMatcher(managerId, new Matcher(matcher.getId(), context, classMatcher, methodMatchers));
				}
			}
			
			@Override
			public void removeMatcher(long id) {
				IClassMatcher matcher = transformer.removeMatcher(managerId, id);
				if(matcher != null) {
					transformService.refreshTransformer(matcher);
				}
			}
			
			@Override
			public void refresh(long matcherId) {
				IClassMatcher matcher = transformer.getMatcher(managerId, matcherId);
				if(matcher != null) {
					transformService.refreshTransformer(matcher);
				}
			}
			
			@Override
			public void refresh() {
				IClassMatcher matcher = transformer.getGroupMatcher(managerId);
				if(matcher != null) {
					transformService.refreshTransformer(matcher);
				}
			}
			
			@Override
			public void restore() {
				IClassMatcher matcher = transformer.removeGroupClassMatcherById(managerId);
				if(matcher != null) {
					transformService.refreshTransformer(matcher);
				}
			}
		});
		
		return managerId;
	}
	
	/**
	 * 注册transformer，并提供切面入口
	 */
	@Override
	public void afterProcessComplete() {
		try {
			transformService.registTransformer(transformer, false);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		Advisor.adviceManager = this;
	}

	
}
