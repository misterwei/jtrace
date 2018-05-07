package com.github.wei.jtrace.core.matchers;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import com.github.wei.jtrace.api.clazz.ClassDescriber;
import com.github.wei.jtrace.api.clazz.IClassDescriberTree;
import com.github.wei.jtrace.api.exception.ClassMatchException;
import com.github.wei.jtrace.api.matcher.IClassMatcher;
import com.github.wei.jtrace.api.matcher.IMatcherAndTransformer;
import com.github.wei.jtrace.core.clazz.MethodDescriber;
import com.github.wei.jtrace.core.util.ClazzUtil;

public class ClassMatcherAndResult implements IMatcherAndTransformer, IQueryMatchResult{
	private IClassMatcher classMatcher;
	private IMethodMatcher[] methodMatchers = null;
	
	private Map<ClassLoader, List<ClassDescriber>> matchedClasses = Collections.synchronizedMap(new HashMap<ClassLoader, List<ClassDescriber>>());
	
	public ClassMatcherAndResult(IClassMatcher classMatcher, IMethodMatcher[] methodMatchers) {
		this.classMatcher = classMatcher;
		this.methodMatchers = methodMatchers;
	}
	
	public ClassMatcherAndResult(IClassMatcher classMatcher) {
		this(classMatcher, null);
	}
	
	public boolean matchClass(IClassDescriberTree descrTree) throws ClassMatchException {
		return classMatcher.matchClass(descrTree);
	}
	
	@Override
	public boolean isMatchSubClass() {
		return classMatcher.isMatchSubClass();
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
	
	public IClassMatcher getClassMatcher() {
		return this.classMatcher;
	}
	
	public Map<ClassLoader, List<ClassDescriber>> getMatchedClasses(){
		return this.matchedClasses;
	}

	/**
	 * 类适配成功后，调用此方法。
	 * 默认进行方法适配
	 */
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		return this.transformer.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
	}

	/**
	 * ClassMatcher适配 并且 MethodMatcher全部适配，才会执行。 
	 * @param loader
	 * @param className
	 * @param classBeingRedefined
	 * @param protectionDomain
	 * @param classfileBuffer
	 * @return
	 * @throws IllegalClassFormatException
	 */
	public byte[] matchedTransform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer, List<MethodDescriber> matchedMethods) throws IllegalClassFormatException {
		return null;
	}
	
	private ClassFileTransformer transformer = new ClassFileTransformer() {
		
		@Override
		public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
				ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
			
			ClassReader cr = new ClassReader(classfileBuffer);
			ClassDescriber descr = ClazzUtil.extractClassDescriber(cr);
			
			final List<MethodDescriber> matchedMethods = new ArrayList<MethodDescriber>();
			
			if(methodMatchers != null && methodMatchers.length > 0) {
				final Set<IMethodMatcher> matchedMatchers = new HashSet<IMethodMatcher>();
				
				cr.accept(new ClassVisitor(Opcodes.ASM5) {
					
					public org.objectweb.asm.MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
						MethodDescriber methodDescr = new MethodDescriber(name, desc, access);
						matchMethod(methodDescr, matchedMatchers, matchedMethods);
						
						return super.visitMethod(access, name, desc, signature, exceptions);
					};
				}, ClassReader.EXPAND_FRAMES);
				
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
						return matchedTransform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer, matchedMethods);
					}
				}
				
			}else {
				recordMethodMatcherMatchedClass(loader, descr);
				return matchedTransform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer, matchedMethods);
			}
			
			// no matched
			return null;
		}
	};


}
