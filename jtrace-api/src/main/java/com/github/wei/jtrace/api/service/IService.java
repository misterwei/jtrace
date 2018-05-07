package com.github.wei.jtrace.api.service;

import com.github.wei.jtrace.api.config.IConfig;

public interface IService {
	
	String getId();
	
	boolean start(IConfig config);
	
}
