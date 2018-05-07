package com.github.wei.jtrace.api.exception;

public class JtraceRuntimeException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JtraceRuntimeException(String message) {
		super(message);
	}
	
	public JtraceRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
	
}
