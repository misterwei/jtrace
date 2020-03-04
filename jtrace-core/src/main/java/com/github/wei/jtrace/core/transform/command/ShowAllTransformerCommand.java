package com.github.wei.jtrace.core.transform.command;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.command.Argument;
import com.github.wei.jtrace.api.command.ICommand;
import com.github.wei.jtrace.core.transform.TransformService;

@Bean
public class ShowAllTransformerCommand implements ICommand{

	@AutoRef
	private TransformService transformerService;
	
	@Override
	public String name() {
		return "transformers";
	}

	@Override
	public Serializable execute(Object... args) throws Exception {
		List<Integer> ids = transformerService.getRegisteredTransformerMatcherIds();
		return new ArrayList<Integer>(ids);
	}

	@Override
	public String introduction() {
		return "显示所有的Transformer ID";
	}

	@Override
	public Argument[] args() {
		return null;
	}

	
}
