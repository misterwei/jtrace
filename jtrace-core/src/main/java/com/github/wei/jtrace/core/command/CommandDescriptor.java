package com.github.wei.jtrace.core.command;

import com.github.wei.jtrace.api.command.Argument;
import com.github.wei.jtrace.api.command.ICommand;
import com.github.wei.jtrace.api.command.ICommandDescriptor;

public class CommandDescriptor implements ICommandDescriptor{
	private static final long serialVersionUID = 1L;
	private ICommand command;
	
	
	public CommandDescriptor(ICommand command) {
		this.command = command;
	}
	
	public String getName() {
		return command.name();
	}
	public String getIntroduction() {
		return command.introduction();
	}
	public Argument[] getArgs() {
		return command.args();
	}
	
	public ICommand getCommand(){
		return command;
	}
	
}
