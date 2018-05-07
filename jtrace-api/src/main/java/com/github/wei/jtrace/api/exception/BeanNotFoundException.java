package com.github.wei.jtrace.api.exception;

public class BeanNotFoundException extends Exception{

	private String beanName;
	
	public BeanNotFoundException(String beanName) {
		super("bean not found [" + beanName + "]");
		this.beanName = beanName;
	}
	
	public BeanNotFoundException(String beanName, String message, Throwable cause) {
		super("bean not found [" + beanName + "]:" + message, cause);
		this.beanName = beanName;
	}

	public BeanNotFoundException(String beanName, String message) {
		super("bean not found [" + beanName + "]:" + message);
		this.beanName = beanName;
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public String getBeanName() {
		return beanName;
	}
}
