package com.github.wei.jtrace.api.exception;

public class ClassFinderException extends Exception{
	private static final long serialVersionUID = 1L;

	public ClassFinderException(String message) {
		super(message);
	}
	
	public ClassFinderException(String message, Throwable thr) {
		super(message, thr);
	}
	
	public ClassFinderException(Throwable thr) {
		super(thr);
	}
}
