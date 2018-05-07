package com.github.wei.jtrace.api.matcher;

import com.github.wei.jtrace.api.clazz.IClassDescriberTree;
import com.github.wei.jtrace.api.exception.ClassMatchException;

public interface IMatcher {
	
	/**
	 * 适配Class
	 * @param loader
	 * @param describer
	 * @return 成功true， 失败false
	 */
	boolean matchClass(IClassDescriberTree describer) throws ClassMatchException;
	
	
	/**
	 * 是否适配子类
	 * @return
	 */
	boolean isMatchSubClass();
}
