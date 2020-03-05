package com.github.wei.jtrace.api.exception;

@SuppressWarnings("serial")
public class BeanProcessException extends Exception {

	/**
	 * Create a new BeanInstantiationException.
	 * @param msg the offending bean class
	 * @param msg the detail message
	 */
	public BeanProcessException(String msg) {
		super(msg);
	}

	public BeanProcessException(Throwable t) {
		super(t);
	}

	/**
	 * Create a new BeanInstantiationException.
	 * @param msg the detail message
	 * @param cause the root cause
	 */
	public BeanProcessException(String msg, Throwable cause) {
		super( msg, cause);
	}


}