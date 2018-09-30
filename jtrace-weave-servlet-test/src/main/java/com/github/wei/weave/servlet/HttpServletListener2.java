package com.github.wei.weave.servlet;

import java.util.Arrays;

import com.github.wei.jtrace.logger.ILogger;
import com.github.wei.jtrace.logger.LoggerFactory;
import com.github.wei.jtrace.weave.api.IWeaveListener;

public class HttpServletListener2 implements IWeaveListener{
	static ILogger logger = LoggerFactory.getLogger(HttpServletListener2.class);
	
	@Override
	public Object[] onBegin(Object[] args) {
		logger.info("maybeSetLastModified : {}", args[1]);
		return null;
	}

	@Override
	public Object onReturn(Object obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onThrow(Throwable thr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onInvoke(Integer lineNumber, String own, String name, String desc, boolean itf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(Class<?> ownClass, Object own, String methodName, String methodDescr) {
		// TODO Auto-generated method stub
		
	}

}
