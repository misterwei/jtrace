package com.github.wei.jtrace.core.logger;

import org.slf4j.Logger;

public class RootLogger {
	static Logger rootLogger = org.slf4j.LoggerFactory.getLogger("jtrace");
	
	public static Logger get(){
		return rootLogger;
	}
}
