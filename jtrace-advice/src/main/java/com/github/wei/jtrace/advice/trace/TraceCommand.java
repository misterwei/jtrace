package com.github.wei.jtrace.advice.trace;

import java.io.Serializable;
import java.util.HashMap;

import com.github.wei.jtrace.advice.AdviceService;
import com.github.wei.jtrace.api.advice.AdviceConfig;
import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.command.Argument;
import com.github.wei.jtrace.api.command.ICommand;

@Bean
public class TraceCommand implements ICommand{

	@AutoRef
	private AdviceService service;
	
	public String name() {
		return "trace";
	}

	public Serializable execute(Object... args) throws Exception {
		String className = String.valueOf(args[0]);
		String method = String.valueOf(args[1]);
		
		int id = service.registAdviceListener(new AdviceConfig(className, method, true), new TraceAdviceListenerManager(service, className), false);

		HashMap<String,Object> result = new HashMap<String, Object>();
		result.put("id", id);
		return result;
	}

	public String introduction() {
		return "代码追踪";
	}

	public Argument[] args() {
		return new Argument[]{new Argument("class", "类名，需要全路径", true, String.class),
				new Argument("method", "方法描述，支持1.方法名适配：method, 2.指定参数个数的方法：method(2), 3.精确适配：method(Ljava/lang/String)V 。多个方法用逗号隔开", true, String.class),
				};
	}

}
