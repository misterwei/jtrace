package com.github.wei.jtrace.api.exception;

public class CommandExecuteException extends Exception{

	private static final long serialVersionUID = 1L;
	
	public CommandExecuteException(String commandName, Throwable e) {
		super("command ["+commandName+"] execute failed",e);
	}

}
