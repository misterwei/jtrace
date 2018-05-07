package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

import com.github.wei.jtrace.core.logger.LoggerFactoryImpl;

public class StaticLoggerBinder implements LoggerFactoryBinder {

	private static StaticLoggerBinder SINGLETON = new StaticLoggerBinder();
	
	private ILoggerFactory loggerFactory = new LoggerFactoryImpl();
	
	public static StaticLoggerBinder getSingleton() {
		return SINGLETON;
	}
	
	@Override
	public ILoggerFactory getLoggerFactory() {
		return loggerFactory;
	}

	@Override
	public String getLoggerFactoryClassStr() {
		return loggerFactory.getClass().getName();
	}

}
