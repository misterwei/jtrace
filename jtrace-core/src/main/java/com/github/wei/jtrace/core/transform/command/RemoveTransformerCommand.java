package com.github.wei.jtrace.core.transform.command;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.command.Argument;
import com.github.wei.jtrace.api.command.ICommand;
import com.github.wei.jtrace.api.matcher.IClassMatcher;
import com.github.wei.jtrace.core.transform.TransformService;
import com.github.wei.jtrace.core.transform.matchers.IMethodMatcher;

@Bean
public class RemoveTransformerCommand implements ICommand{

	@AutoRef
	private TransformService transformerService;
	
	@Override
	public String name() {
		return "removeTransformer";
	}

	@Override
	public String introduction() {
		return "删除一个Transformer";
	}
	
	@Override
	public Serializable execute(Object... args) throws Exception {
		Integer id = (Integer)args[0];
		transformerService.removeTransformerById(id);
		
		return "ok";
	}
	
	@Override
	public Argument[] args() {
		return new Argument[] {new Argument("id", "transformer id", true, Integer.class) };
	}
}
