package com.github.wei.jtrace.core.logger;

public class ConsoleAppender implements IAppender{

	public static final ConsoleAppender INSTANCE = new ConsoleAppender();
	
	@Override
	public void open() {
		
	}

	@Override
	public void append(String msg) {
		System.out.println(msg);
	}

	@Override
	public void close() {
		
	}

}
