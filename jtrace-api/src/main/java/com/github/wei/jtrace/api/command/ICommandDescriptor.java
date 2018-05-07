package com.github.wei.jtrace.api.command;

import java.io.Serializable;

public interface ICommandDescriptor extends Serializable{
	String getName();
	String getIntroduction();
	Argument[] getArgs();
}
