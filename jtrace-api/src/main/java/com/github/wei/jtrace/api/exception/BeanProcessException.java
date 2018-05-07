package com.github.wei.jtrace.api.exception;

@SuppressWarnings("serial")
public class BeanProcessException extends Exception {

	/**
	 * Create a new BeanInstantiationException.
	 * @param beanClass the offending bean class
	 * @param msg the detail message
	 */
	public BeanProcessException(String msg) {
		super(msg);
	}

	/**
	 * Create a new BeanInstantiationException.
	 * @param beanClass the offending bean class
	 * @param msg the detail message
	 * @param cause the root cause
	 */
	public BeanProcessException(String msg, Throwable cause) {
		super( msg, cause);
	}


}