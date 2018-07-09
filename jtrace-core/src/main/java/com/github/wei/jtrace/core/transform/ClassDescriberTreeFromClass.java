package com.github.wei.jtrace.core.transform;

import com.github.wei.jtrace.api.clazz.ClassDescriber;
import com.github.wei.jtrace.api.clazz.IClassDescriberTree;
import com.github.wei.jtrace.core.util.ClazzUtil;
import com.github.wei.jtrace.core.util.Constants;

public class ClassDescriberTreeFromClass implements IClassDescriberTree{
	private Class<?> clazz;
	private ClassDescriber classDescriber;
	private IClassDescriberTree[] interfaces;
	
	public ClassDescriberTreeFromClass(Class<?> clazz) {
		this.clazz = clazz;
		this.classDescriber = ClazzUtil.extractClassDescriber(clazz);
	}
	
	@Override
	public ClassDescriber getClassDescriber() {
		return classDescriber;
	}

	@Override
	public IClassDescriberTree getSuperClass() {
		if(ClazzUtil.classNameEquals(classDescriber.getName(), Constants.CLASS_OBJECT)) {
			return null;
		}
		
		Class<?> superClass = clazz.getSuperclass();
		if(superClass != null) {
			return new ClassDescriberTreeFromClass(superClass);
		}
		return null;
	}
	
	@Override
	public IClassDescriberTree[] getInterfaces() {
		if(ClazzUtil.classNameEquals(classDescriber.getName(), Constants.CLASS_OBJECT)) {
			return null;
		}
		if(interfaces != null) {
			return interfaces;
		}
		
		Class<?>[] itfs = clazz.getInterfaces();
		if(itfs == null) {
			return null;
		}
		
		interfaces = new ClassDescriberTreeFromClass[itfs.length];

		for(int i=0;i<itfs.length;i++) {
			Class<?> itf = itfs[i];
			interfaces[i] = new ClassDescriberTreeFromClass(itf);
		}
		
		return interfaces;	
	}

}
