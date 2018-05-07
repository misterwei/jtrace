package com.github.wei.jtrace.core.command;

import java.io.Serializable;

import com.github.wei.jtrace.api.command.ICommandResult;

public class CommandResult implements ICommandResult {
	private boolean success;
	private String message;
	private Serializable result;
	
	public CommandResult(boolean success) {
		this.success = success;
	}
	
	public CommandResult(boolean success, String message) {
		this.success = success;
		this.message = message;
	}
	
	@Override
	public boolean isSuccess() {
		return success;
	}

	@Override
	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setResult(Serializable result) {
		this.result = result;
	}

	@Override
	public Serializable getResult() {
		return this.result;
	}

}
