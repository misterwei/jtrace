package com.github.wei.jtrace.core.transform;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.api.clazz.IClassDescriberTree;
import com.github.wei.jtrace.api.exception.ClassMatchException;
import com.github.wei.jtrace.api.matcher.IClassMatcher;
import com.github.wei.jtrace.api.matcher.ITransformer;
import com.github.wei.jtrace.core.clazz.MethodDescriber;
import com.github.wei.jtrace.core.transform.matchers.IMethodMatcher;
import com.github.wei.jtrace.core.util.ClazzUtil;

public abstract class AbstractMatcherAndTransformer implements ITransformer{
	private static Logger log = LoggerFactory.getLogger("ClassMatcherAndTransformer");

	private ConcurrentHashMap<IClassMatcher, List<IMethodMatcher>> matchers = new ConcurrentHashMap<IClassMatcher, List<IMethodMatcher>>();

	public abstract byte[] matchedTransform(final ClassLoader loader, IClassDescriberTree descr, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer, Set<MethodDescriber> matchedMethods) throws IllegalClassFormatException;
	
	@Override
	public byte[] transform(ClassLoader loader, IClassDescriberTree descr, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		ClassReader classReader = new ClassReader(classfileBuffer);
		List<MethodDescriber> methods = ClazzUtil.extractMethodDescribers(classReader);
		
		Set<MethodDescriber> matchedMethods = new HashSet<MethodDescriber>();
		try {
			if(isMatched(descr, methods, matchedMethods)) {
				return matchedTransform(loader, descr, classBeingRedefined, protectionDomain, classfileBuffer, matchedMethods);
			}
			
		}catch (ClassMatchException e) {
			log.error("Match class " + descr.getClassDescriber().getName() +" failed", e);
		}
		
		return null;
	}

	private boolean isMatched(IClassDescriberTree descr, List<MethodDescriber> methods, Set<MethodDescriber> matchResult) throws ClassMatchException{
		Set<IClassMatcher> classMatchers = matchers.keySet();
		boolean matched = true;
		for(IClassMatcher matcher: classMatchers) {
			if(matcher.matchClass(descr)) {
				List<IMethodMatcher> methodMatchers = matchers.get(matcher);
				//如果MethodMatchers是空，意味着全部适配
				if(methodMatchers == null || methodMatchers.isEmpty()) {
					matchResult.addAll(methods);
					return true;
				}else {
					if(methods != null && !methods.isEmpty()) {
						List<MethodDescriber> matchedMethod = new ArrayList<MethodDescriber>();
						boolean innerMatched = true;
						
						for(IMethodMatcher methodMatcher : methodMatchers) {
							List<MethodDescriber> innerMatchedMethod = new ArrayList<MethodDescriber>();
							for(MethodDescriber m : methods) {
								if(methodMatcher.match(m)) {
									innerMatchedMethod.add(m);
								}
							}
							if(innerMatchedMethod.size() == 0) {
								innerMatched = false;
								matched = false;
								break;
							}else {
								matchedMethod.addAll(innerMatchedMethod);
							}
						}
						if(innerMatched) {
							matchResult.addAll(matchedMethod);
						}
					}
				}
			}
		}
		
		return matched;
	}
	
	@Override
	public boolean needRetransform(IClassDescriberTree descr) {
		try {
			Set<IClassMatcher> classMatchers = matchers.keySet();
			for(IClassMatcher matcher: classMatchers) {
				if(matcher.matchClass(descr)) {
					return true;
				}
			}
		}catch(ClassMatchException e) {
			log.error("Match class " + descr.getClassDescriber().getName() + " failed", e);
		}
		return false;
	}
	
	public void addMatcher(IClassMatcher classMatcher, List<IMethodMatcher> methodMatchers) {
		List<IMethodMatcher> _methodMatchers = matchers.get(classMatcher);
		if(_methodMatchers == null) {
			matchers.putIfAbsent(classMatcher, new CopyOnWriteArrayList<IMethodMatcher>());
			_methodMatchers = matchers.get(classMatcher);
		}
		_methodMatchers.addAll(methodMatchers);
	}
}
