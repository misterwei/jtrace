package com.github.wei.weave.servlet;

import java.util.Arrays;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.github.wei.jtrace.weave.api.IWeaveListener;

public class HttpServletListener implements IWeaveListener{

	@Override
	public Object[] onBegin(Object[] args) {
		HttpServletRequest request = (HttpServletRequest)args[0];
		Map<String, String[]> params = request.getParameterMap();
		System.out.println("HttpServletListener ----------------- ");
		for(Map.Entry<String, String[]> entry : params.entrySet()) {
			System.out.println("parameter " +entry.getKey() + " = " + Arrays.toString(entry.getValue()));
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
