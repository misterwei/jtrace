package com.github.wei.jtrace.core.service;

import com.github.wei.jtrace.api.service.IService;

public interface IServiceManager {

	void start();
	
	boolean registAndStart(IService service);
	
	void stop();
	
	void removeAndStop(String id);
	
	<T extends IService> T getService(String id);
}
