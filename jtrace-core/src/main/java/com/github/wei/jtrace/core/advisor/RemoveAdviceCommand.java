package com.github.wei.jtrace.core.advisor;

import java.io.Serializable;

import com.github.wei.jtrace.api.advice.IAdviceListenerManager;
import com.github.wei.jtrace.api.advice.IAdviceManager;
import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.command.Argument;
import com.github.wei.jtrace.api.command.ICommand;

@Bean
public class RemoveAdviceCommand implements ICommand{

	@AutoRef
	private IAdviceManager adviceManager;
	
	@Override
	public String name() {
		return "removeAdvice";
	}

	@Override
	public Serializable execute(Object... args) throws Exception {
		Integer id = (Integer)args[0];
		IAdviceListenerManager manager = adviceManager.removeAdviceListener(id);
		return String.valueOf(manager);
	}

	@Override
	public String introduction() {
		return "删除Advice或Weave";
	}

	@Override
	public Argument[] args() {
		return new Argument[] {
				new Argument("id", "advice或weave id", true, Integer.class)
		};
	}

}
