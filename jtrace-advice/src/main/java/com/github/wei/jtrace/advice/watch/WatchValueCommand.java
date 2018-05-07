package com.github.wei.jtrace.advice.watch;

import java.io.Serializable;
import java.util.HashMap;

import com.github.wei.jtrace.advice.AdviceService;
import com.github.wei.jtrace.api.advice.AdviceConfig;
import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.command.Argument;
import com.github.wei.jtrace.api.command.ICommand;

@Bean
public class WatchValueCommand implements ICommand{

	@AutoRef
	private AdviceService adviceService;
	
	public String name() {
		return "watch";
	}

	public Serializable execute(Object... args) throws Exception {
		String className = String.valueOf(args[0]);
		String method = String.valueOf(args[1]);
		String pos = String.valueOf(args[2]);
		String expr = String.valueOf(args[3]);
		
		AdviceConfig config = new AdviceConfig(className, method);
		int id = adviceService.registAdviceListener(config, new WatchValueAdviceListenerManager(pos, expr), false);
		
		HashMap<String,Object> result = new HashMap<String, Object>();
		result.put("id", id);
		return result;
	}

	public String introduction() {
		return "观察参数值";
	}

	public Argument[] args() {
		return new Argument[]{new Argument("class", "类名，需要全路径", true, String.class),
				new Argument("method", "方法描述，支持1.方法名适配：method, 2.指定参数个数的方法：method(2), 3.精确适配：method(Ljava/lang/String)V 。多个方法用逗号隔开", true, String.class),
				new Argument("pos", "观察位置，支持1.开始：begin, 2.返回：return, 3.异常：throw", true, String.class),
				new Argument("expr", "OGNL表达式", true, String.class)
				};
	}

	
}
