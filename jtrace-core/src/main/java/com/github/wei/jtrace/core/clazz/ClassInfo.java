package com.github.wei.jtrace.core.clazz;

import java.io.Serializable;
import java.util.List;

import com.github.wei.jtrace.api.clazz.ClassDescriber;

public class ClassInfo implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ClassDescriber classDescriber;
	
	private String classLoader;
	
	private List<MethodDescriber> methods;
	
	public String getClassLoader() {
		return classLoader;
	}
	public void setClassLoader(String classLoader) {
		this.classLoader = classLoader;
	}
	
	public List<MethodDescriber> getMethods() {
		return methods;
	}
	public void setMethods(List<MethodDescriber> methods) {
		this.methods = methods;
	}
	
	public ClassDescriber getClassDescriber() {
		return classDescriber;
	}
	public void setClassDescriber(ClassDescriber classDescriber) {
		this.classDescriber = classDescriber;
	}
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
