package com.github.wei.jtrace.core.logger;

import java.util.ArrayList;
import java.util.List;

public class AllAppender implements IAppender{
	private List<IAppender> appenders = new ArrayList<IAppender>();

	public void addAppender(IAppender appender){
		appenders.add(appender);
	}
	
	@Override
	public void open() throws Exception{
		for(IAppender appender : appenders){
			appender.open();
		}
	}

	@Override
	public void append(String msg) {
		for(IAppender appender : appenders){
			appender.append(msg);
		}
	}

	@Override
	public void close() {
		for(IAppender appender : appenders){
			appender.close();
		}
	}
	
}
