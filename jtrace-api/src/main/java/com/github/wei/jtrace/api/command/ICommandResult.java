package com.github.wei.jtrace.api.command;

import java.io.Serializable;

public interface ICommandResult {
	boolean isSuccess();
	
	String getMessage();
	
	Serializable getResult();
}
