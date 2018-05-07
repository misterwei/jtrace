package com.github.wei.jtrace.core.logger;

import java.io.Closeable;

public interface IAppender extends Closeable {
		
	void open() throws Exception;
	
	void append(String msg);
	
	void close();
}
