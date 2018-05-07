package com.github.wei.jtrace.api.exception;

public class ClassMatchException extends Exception{
	private static final long serialVersionUID = 1L;

	public ClassMatchException(String message, Throwable thr) {
		super(message, thr);
	}
	
	public ClassMatchException(Throwable thr) {
		super(thr);
		
	}
}
