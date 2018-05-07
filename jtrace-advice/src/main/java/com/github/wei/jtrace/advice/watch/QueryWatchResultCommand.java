package com.github.wei.jtrace.advice.watch;

import java.io.Serializable;
import java.util.ArrayList;

import com.github.wei.jtrace.advice.AdviceService;
import com.github.wei.jtrace.api.advice.IAdviceListenerManager;
import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.command.Argument;
import com.github.wei.jtrace.api.command.ICommand;

@Bean
public class QueryWatchResultCommand implements ICommand{

	@AutoRef
	private AdviceService adviceService;
	
	public String name() {
		return "queryWatch";
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
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
			throw new Exception("没有找到对应的Watch结果");
		}
		if(!(listener instanceof WatchValueAdviceListenerManager)) {
			throw new Exception("查到的IAdviceListener不是WatchValueAdviceListener");
		}
		WatchValueAdviceListenerManager watch = (WatchValueAdviceListenerManager)listener;
		
		return new ArrayList(watch.getResult());
	}

	public String introduction() {
		return "观察参数值";
	}

	public Argument[] args() {
		return new Argument[]{new Argument("id", "watch命令返回的ID", true, Integer.class),
				new Argument("hold", "是否继续持有这个结果", Boolean.class, false)};
	}

	
}
