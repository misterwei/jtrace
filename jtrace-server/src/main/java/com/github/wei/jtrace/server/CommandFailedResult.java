package com.github.wei.jtrace.server;

import java.io.Serializable;

import com.github.wei.jtrace.api.command.ICommandResult;

public class CommandFailedResult implements ICommandResult, Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private	Serializable exception;
	private boolean success;
	private String message;
	
	public CommandFailedResult(String command) {
		this.success = false;
		this.message = "command ("+command+") execute failed";
	}
	
	public CommandFailedResult(String command, String message) {
		this(command);
		this.exception = message;
	}
	
	public CommandFailedResult(String command, Throwable e) {
		this(command);
		this.exception = e;
	}
	
	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	public Serializable getResult() {
		return exception;
	}

}
