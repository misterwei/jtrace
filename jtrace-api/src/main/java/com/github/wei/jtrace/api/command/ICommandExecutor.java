package com.github.wei.jtrace.api.command;

public interface ICommandExecutor {
	
	ICommandResult execute(String command, Object... args);
	
	ICommandResult execute(ICommandDescriptor commandDescriptor, Object... args);
	
	ICommandDescriptor findCommand(String command);
}
