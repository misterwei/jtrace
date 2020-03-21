package com.github.wei.jtrace.asm.command;

import com.github.wei.jtrace.api.clazz.ClassDescriber;
import com.github.wei.jtrace.api.clazz.IClassDescriberTree;
import com.github.wei.jtrace.api.exception.ClassMatchException;
import com.github.wei.jtrace.api.exception.TransformException;
import com.github.wei.jtrace.api.transform.ITransformer;
import com.github.wei.jtrace.api.transform.ITransformerMatcher;
import com.github.wei.jtrace.api.transform.matcher.IClassMatcher;
import com.github.wei.jtrace.asm.api.matcher.IMethodMatcher;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClassMatcherAndResult implements ITransformerMatcher, ITransformer, IQueryMatchResult {
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

	private void matchMethod(MethodNode descr, Map<IMethodMatcher, Boolean> matchers) {
		for(IMethodMatcher matcher : methodMatchers) {
			if(matcher.match(descr)) {
				matchers.put(matcher, true);
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

	@Override
	public ITransformer matchedTransformer(IClassDescriberTree classTree) throws ClassMatchException {
		if(classMatcher.matchClass(classTree)){
			return this;
		}
		return null;
	}

	@Override
	public byte[] transform(ClassLoader loader, IClassDescriberTree descr,
							Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
							byte[] classfileBuffer) throws TransformException {
		ClassReader cr = new ClassReader(classfileBuffer);
		ClassNode classNode = new ClassNode();
		cr.accept(classNode, ClassReader.EXPAND_FRAMES);

		Map<IMethodMatcher, Boolean> methodMatcherResult = new HashMap<IMethodMatcher, Boolean>();
		List<MethodNode> methodNodes =  classNode.methods;
		for(MethodNode mn : methodNodes){
			matchMethod(mn, methodMatcherResult);
		}

		for(IMethodMatcher matcher : methodMatchers){
			if(methodMatcherResult.get(matcher) == null){
				return null;
			}
		}

		recordMethodMatcherMatchedClass(loader, descr.getClassDescriber());
		return null;
	}
}
