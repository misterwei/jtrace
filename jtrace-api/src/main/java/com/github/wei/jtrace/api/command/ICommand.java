package com.github.wei.jtrace.api.command;

import java.io.Serializable;

public interface ICommand {
	String name();
	
	Serializable execute(Object... args) throws Exception;
	
	String introduction();
	
	Argument[] args();
}
