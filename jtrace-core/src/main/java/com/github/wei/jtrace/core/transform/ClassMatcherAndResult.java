package com.github.wei.jtrace.core.transform;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.api.clazz.ClassDescriber;
import com.github.wei.jtrace.api.clazz.IClassDescriberTree;
import com.github.wei.jtrace.api.clazz.MethodDescriber;
import com.github.wei.jtrace.api.exception.ClassMatchException;
import com.github.wei.jtrace.api.transform.ITransformer;
import com.github.wei.jtrace.api.transform.matcher.IClassMatcher;
import com.github.wei.jtrace.api.transform.matcher.IMethodMatcher;
import com.github.wei.jtrace.core.transform.matchers.IQueryMatchResult;
import com.github.wei.jtrace.core.util.ClazzUtil;

public class ClassMatcherAndResult implements ITransformer,IQueryMatchResult{
	private static Logger log = LoggerFactory.getLogger("ClassMatcherAndResult");
	
	private ConcurrentHashMap<ClassLoader, List<ClassDescriber>> matchedClasses = new ConcurrentHashMap<ClassLoader, List<ClassDescriber>>();
	
	private IClassMatcher classMatcher;
	private IMethodMatcher[] methodMatchers;
	
	public ClassMatcherAndResult(IClassMatcher matcher) {
		this.classMatcher = matcher;
	}
	
	public ClassMatcherAndResult(IClassMatcher matcher, IMethodMatcher[] methodMatchers) {
		this.classMatcher = matcher;
		this.methodMatchers = methodMatchers;
	}
	
	@Override
	public boolean matchClass(IClassDescriberTree descr) throws ClassMatchException {
		return classMatcher.matchClass(descr);
	}

	@Override
	public Map<ClassLoader, List<ClassDescriber>> getMatchedClasses() {
		return matchedClasses;
	}

	@Override
	public byte[] transform(ClassLoader loader, IClassDescriberTree descrTree, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		try {
			if(!classMatcher.matchClass(descrTree)) {
				return null;
			}
		}catch(ClassMatchException e) {
			log.error("Transform match class " + descrTree.getClassDescriber().getName() + " failed", e);
			return null;
		}
		
		ClassReader cr = new ClassReader(classfileBuffer);
		ClassDescriber descr = descrTree.getClassDescriber();
		
		final List<MethodDescriber> matchedMethods = new ArrayList<MethodDescriber>();
		
		if(methodMatchers != null && methodMatchers.length > 0) {
			final Set<IMethodMatcher> matchedMatchers = new HashSet<IMethodMatcher>();
			
			List<MethodDescriber> mds = ClazzUtil.extractMethodDescribers(cr);
			for(MethodDescriber md : mds) {
				matchMethod(md, matchedMatchers, matchedMethods);
			}
			
			//必须所有方法适配，才被认为是Class适配
			if(methodMatchers.length == matchedMatchers.size()) {
				boolean isMatched = true;
				for(int i=0;i<methodMatchers.length; i++) {
					if(!matchedMatchers.contains(methodMatchers[i])) {
						isMatched = false;
					}
				}
				
				if(isMatched) {
					recordMethodMatcherMatchedClass(loader, descr);
					return null;
				}
			}
			
		}else {
			recordMethodMatcherMatchedClass(loader, descr);
		}
		
		return null;
	}

	private void matchMethod(MethodDescriber descr, Set<IMethodMatcher> matchers, List<MethodDescriber> descrs) {
		boolean matched = false;
		for(IMethodMatcher matcher : methodMatchers) {
			if(matcher.match(descr)) {
				matchers.add(matcher);
				if(!matched) {
					matched = true;
					descrs.add(descr);
				}
			}
		}
	}
	
	private  void recordMethodMatcherMatchedClass(ClassLoader loader, ClassDescriber descr) {
		List<ClassDescriber> descrList = matchedClasses.get(loader);
		if(descrList == null) {
			synchronized (matchedClasses) {
				if(descrList == null) {
					descrList = new CopyOnWriteArrayList<ClassDescriber>();
					matchedClasses.put(loader, descrList);
				}
			}
		}
		
		if(!descrList.contains(descr)) {
			descrList.add(descr);
		}
	}
	
}
