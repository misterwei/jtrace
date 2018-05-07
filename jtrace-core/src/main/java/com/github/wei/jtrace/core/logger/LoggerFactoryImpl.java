package com.github.wei.jtrace.core.logger;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

public class LoggerFactoryImpl implements ILoggerFactory{

	public Logger getLogger(String name) {
		return new SimpleFormatLogger(name);
	}
	
}
