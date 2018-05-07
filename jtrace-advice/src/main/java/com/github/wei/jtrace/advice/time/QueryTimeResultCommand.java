package com.github.wei.jtrace.advice.time;

import java.io.Serializable;

import com.github.wei.jtrace.advice.AdviceService;
import com.github.wei.jtrace.api.advice.IAdviceListenerManager;
import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.command.Argument;
import com.github.wei.jtrace.api.command.ICommand;

@Bean
public class QueryTimeResultCommand implements ICommand{

	@AutoRef
	private AdviceService adviceService;
	
	public String name() {
		return "queryTime";
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
			throw new Exception("没有找到对应的Time结果");
		}
		if(!(listener instanceof TimeCountAdviceListenerManager)) {
			throw new Exception("查到的IAdviceListener不是TimeCountAdviceListenerManager");
		}
		TimeCountAdviceListenerManager watch = (TimeCountAdviceListenerManager)listener;
		
		return watch.computeTimeCount();
	}

	public String introduction() {
		return "观察执行时间统计结果";
	}

	public Argument[] args() {
		return new Argument[]{new Argument("id", "time命令返回的ID", true, Integer.class),
				new Argument("hold", "是否继续持有这个结果", Boolean.class, false)};
	}

	
}
