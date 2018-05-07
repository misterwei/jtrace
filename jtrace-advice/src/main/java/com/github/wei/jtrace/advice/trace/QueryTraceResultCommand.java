package com.github.wei.jtrace.advice.trace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.github.wei.jtrace.advice.AdviceService;
import com.github.wei.jtrace.api.advice.IAdviceListenerManager;
import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.command.Argument;
import com.github.wei.jtrace.api.command.ICommand;

@Bean
public class QueryTraceResultCommand implements ICommand{

	@AutoRef
	private AdviceService adviceService;
	
	public String name() {
		return "queryTrace";
	}

	public Serializable execute(Object... args) throws Exception {
		int id = Integer.parseInt(String.valueOf(args[0]));
		boolean hold = Boolean.getBoolean(String.valueOf(args[1]));
		
		IAdviceListenerManager listener = null;
		if(hold) {
			listener = adviceService.getAdviceListenerManager(id);
		}else {
			listener = adviceService.removeAdviceListenerManager(id);
		}
		
		if(listener == null) {
			throw new Exception("没有找到对应的Trace结果");
		}
		if(!(listener instanceof TraceAdviceListenerManager)) {
			throw new Exception("查到的IAdviceListener不是TraceAdviceListenerManager");
		}
		TraceAdviceListenerManager trace = (TraceAdviceListenerManager)listener;
		
		ArrayList<ActionData> datas = new ArrayList<ActionData>();
		
		List<Action> actions = trace.getActions();
		if(actions != null) {
			for(Action action : actions) {
				datas.add(new ActionData(action));
			}
		}
		
		return datas;
	}

	public String introduction() {
		return "查询跟踪情况";
	}

	public Argument[] args() {
		return new Argument[]{new Argument("id", "trace命令返回的ID", true, Integer.class),
				new Argument("hold", "是否继续持有这个结果, 默认false", Boolean.class, false)};
	}

	
}
