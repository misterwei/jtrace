package com.github.wei.jtrace.api.advice;

public class AdviceConfig {
	private String className;	
	private String methods;
	private boolean invokeTrace;
	
	public AdviceConfig(String className) {
		this(className, null, false);
	}
	
	public AdviceConfig(String className,String methods) {
		this(className, methods, false);
	}
	
	public AdviceConfig(String className, String methods, boolean invokeTrace) {
		this.className = className;
		this.methods = methods;
		this.invokeTrace = invokeTrace;
	}
	
	public String getClassName() {
		return className;
	}

	public String getMethods() {
		return methods;
	}

	public boolean isInvokeTrace() {
		return invokeTrace;
	}

}
