package com.github.wei.jtrace.core.transform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.api.clazz.ClassDescriber;
import com.github.wei.jtrace.api.clazz.IClassDescriberTree;
import com.github.wei.jtrace.api.clazz.IClassFinder;
import com.github.wei.jtrace.api.exception.ClassFinderException;
import com.github.wei.jtrace.core.util.ClazzUtil;
import com.github.wei.jtrace.core.util.Constants;

public class ClassDescriberTreeFromClassFinder implements IClassDescriberTree{
	static Logger log = LoggerFactory.getLogger(ClassDescriberTreeFromClassFinder.class);
	private IClassFinder classFinder;
	private ClassDescriber classDescriber;
	private String className;
	private IClassDescriberTree[] interfaces;

	
	public ClassDescriberTreeFromClassFinder(IClassFinder classFinder, ClassDescriber classDescriber) {
		this.classDescriber = classDescriber;
		this.classFinder = classFinder;
		this.className = classDescriber.getName();
	}
	
	public ClassDescriberTreeFromClassFinder(IClassFinder classFinder, String className) {
		this.className = className;
		this.classFinder = classFinder;
	}
	
	@Override
	public ClassDescriber getClassDescriber() {
		if(classDescriber != null) {
			return classDescriber;
		}
		
		try {
			classDescriber = classFinder.find(className);
		} catch (ClassFinderException e) {
			throw new RuntimeException(e);
		}
		
		return classDescriber;
	}
	
	@Override
	public IClassDescriberTree getSuperClass() {
		if(ClazzUtil.classNameEquals(getClassDescriber().getName(), Constants.CLASS_OBJECT)) {
			return null;
		}
		return new ClassDescriberTreeFromClassFinder(classFinder, getClassDescriber().getSuperClass());
	}

	@Override
	public IClassDescriberTree[] getInterfaces() {
		if(ClazzUtil.classNameEquals(getClassDescriber().getName(), Constants.CLASS_OBJECT)) {
			return null;
		}
		if(interfaces != null) {
			return interfaces;
		}
		
		String[] itfs = getClassDescriber().getInterfaces();
		if(itfs == null) {
			return null;
		}
		
		interfaces = new ClassDescriberTreeFromClassFinder[itfs.length];
		
		for(int i=0;i<itfs.length;i++) {
			String itf = itfs[i];
			interfaces[i] = new ClassDescriberTreeFromClassFinder(classFinder, itf);
		}
		
		return interfaces;			
	}

	
}
