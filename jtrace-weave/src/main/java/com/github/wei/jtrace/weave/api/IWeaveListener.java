package com.github.wei.jtrace.weave.api;

import com.github.wei.jtrace.api.advice.IAdviceListener;

public interface IWeaveListener extends IAdviceListener{

	public void init(Class<?> ownClass, Object own, String methodName, String methodDescr);
}
