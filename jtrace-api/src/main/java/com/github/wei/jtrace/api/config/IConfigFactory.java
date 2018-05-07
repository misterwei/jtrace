package com.github.wei.jtrace.api.config;

public interface IConfigFactory {

	<T> T getConfig(Class<T> type) throws Exception;
	
	IConfig getConfig(String path);
}
