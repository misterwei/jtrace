package com.github.wei.jtrace.core.test.config;

import com.github.wei.jtrace.api.config.Config;
import com.github.wei.jtrace.api.config.ConfigPath;
import com.github.wei.jtrace.api.config.ConfigValue;

@Config("socket")
public class SocketConfig {

	@ConfigPath
	private int timeout;

	@ConfigPath
	@ConfigValue("2")
	private int connections;
	
	public int getTimeout() {
		return timeout;
	}
	
	public int getConnections() {
		return connections;
	}
}
