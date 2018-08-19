package com.github.wei.jtrace.logger;

import org.slf4j.Logger;

public class LoggerFactory {

	public static ILogger getLogger(String name) {
		Logger log = org.slf4j.LoggerFactory.getLogger(name);
		return new LoggerImpl(log);
	}
	
	public static ILogger getLogger(Class<?> clzz) {
		Logger log = org.slf4j.LoggerFactory.getLogger(clzz);
		return new LoggerImpl(log);
	}
}
