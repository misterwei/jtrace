package com.github.wei.jtrace.api.advice;

public interface IAdviceListener {
	/**
	 * 方法开始
	 * @param ownClass
	 * @param own
	 * @param methodName
	 * @param methodDescr
	 * @param args 方法的参数
	 */
	void onBegin(Object[] args);
	
	/**
	 * 方法返回
	 * @param ownClass
	 * @param own
	 * @param methodName
	 * @param methodDescr
	 * @param obj 返回值
	 */
	void onReturn(Object obj);
	
	/**
	 * 方法异常
	 * @param ownClass
	 * @param own
	 * @param methodName
	 * @param methodDescr
	 * @param thr 异常信息
	 */
	void onThrow(Throwable thr);

	/**
	 * 执行代码
	 * @param lineNumber
	 * @param own
	 * @param name
	 * @param desc
	 */
	void onInvoke(Integer lineNumber, String own, String name, String desc, boolean itf);
}
