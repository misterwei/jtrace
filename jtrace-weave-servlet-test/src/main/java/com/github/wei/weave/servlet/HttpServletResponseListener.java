package com.github.wei.weave.servlet;

import com.github.wei.jtrace.logger.ILogger;
import com.github.wei.jtrace.logger.LoggerFactory;
import com.github.wei.jtrace.weave.api.IWeaveListener;

public class HttpServletResponseListener implements IWeaveListener{
	static ILogger log = LoggerFactory.getLogger("HttpServletResponseListener");
	
	@Override
	public Object[] onBegin(Object[] args) {
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
		log.info("---- {}.{}", new Object[] {ownClass.getName(), methodName});
	}

}
