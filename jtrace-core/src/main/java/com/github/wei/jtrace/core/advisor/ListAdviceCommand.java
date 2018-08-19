package com.github.wei.jtrace.core.advisor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.github.wei.jtrace.api.advice.IAdviceListenerManager;
import com.github.wei.jtrace.api.advice.IAdviceManager;
import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.command.Argument;
import com.github.wei.jtrace.api.command.ICommand;

@Bean
public class ListAdviceCommand implements ICommand{

	@AutoRef
	private IAdviceManager adviceManager;
	
	@Override
	public String name() {
		return "advices";
	}

	@Override
	public Serializable execute(Object... args) throws Exception {
		HashMap<Integer, String> result = new HashMap<Integer, String>();
		Map<Integer, IAdviceListenerManager > listeners = adviceManager.getAdviceListeners();
		for(Map.Entry<Integer, IAdviceListenerManager> entry : listeners.entrySet()) {
			result.put(entry.getKey(), String.valueOf(entry.getValue()));
		}
		return result;
	}

	@Override
	public String introduction() {
		return "显示所有的Advice或Weave";
	}

	@Override
	public Argument[] args() {
		return new Argument[0];
	}

}
