package com.github.wei.weave.servlet;

import java.util.Arrays;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.github.wei.jtrace.logger.ILogger;
import com.github.wei.jtrace.logger.LoggerFactory;
import com.github.wei.jtrace.weave.api.IWeaveListener;

public class HttpServletListener implements IWeaveListener{
	static ILogger logger = LoggerFactory.getLogger(HttpServletListener.class);
	
	@Override
	public Object[] onBegin(Object[] args) {
		HttpServletRequest request = (HttpServletRequest)args[0];
		Map<String, String[]> params = request.getParameterMap();
		logger.info("HttpServletListener onBegin ");
		for(Map.Entry<String, String[]> entry : params.entrySet()) {
			logger.info("parameter " +entry.getKey() + " = " + Arrays.toString(entry.getValue()));
		}
		return args;
	}

	@Override
	public Object onReturn(Object obj) {
		return obj;
	}

	@Override
	public void onThrow(Throwable thr) {
		
	}

	@Override
	public void onInvoke(Integer lineNumber, String own, String name, String desc, boolean itf) {
		
	}

	@Override
	public void init(Class<?> ownClass, Object own, String methodName, String methodDescr) {
		
	}

}
