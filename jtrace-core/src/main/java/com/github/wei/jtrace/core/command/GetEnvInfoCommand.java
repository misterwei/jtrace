package com.github.wei.jtrace.core.command;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.github.wei.jtrace.api.command.Argument;
import com.github.wei.jtrace.api.command.ICommand;

public class GetEnvInfoCommand implements ICommand {

	@Override
	public String name() {
		return "env";
	}

	@Override
	public Serializable execute(Object...args) throws Exception {
		Map<String,String> env = System.getenv();
		Properties p = System.getProperties();
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put("env", env);
		result.put("properties", p);
		
		return result;
	}

	@Override
	public Argument[] args() {
		return new Argument[0];
	}

	@Override
	public String introduction() {
		return "获取系统环境信息";
	}
	
	

}
