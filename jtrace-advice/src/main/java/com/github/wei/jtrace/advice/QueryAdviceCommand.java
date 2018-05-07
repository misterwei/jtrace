package com.github.wei.jtrace.advice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.command.Argument;
import com.github.wei.jtrace.api.command.ICommand;

@Bean
public class QueryAdviceCommand implements ICommand{

	@AutoRef
	private AdviceService service;
	
	public String name() {
		return "queryAdvice";
	}

	public Serializable execute(Object... args) throws Exception {
		Set<Integer> ids = service.getRegistIds();
		return new ArrayList<Integer>(ids);
	}

	public String introduction() {
		return "查询目前注册的切面";
	}

	public Argument[] args() {
		return null;
	}

	
}
