package com.github.wei.jtrace.core.test.config;

import com.github.wei.jtrace.api.config.Config;
import com.github.wei.jtrace.api.config.ConfigPath;
import com.github.wei.jtrace.api.config.ConfigValue;

@Config
public class DefaultConfig {
	
	@ConfigPath("socket.timeout")
	@ConfigValue("500")
	private int timeout;
	
	@ConfigPath("server.port")
	@ConfigValue("3456")
	private int serverPort;

	
	
	public int getTimeout() {
		return timeout;
	}

	public int getServerPort() {
		return serverPort;
	} 
	
}
