package com.github.wei.jtrace.core.service;

import com.github.wei.jtrace.api.service.IService;
import com.github.wei.jtrace.api.exception.ServiceStartException;
import com.github.wei.jtrace.api.exception.ServiceStopException;
import com.github.wei.jtrace.core.exception.ServiceAlreadyExistsException;

public interface IServiceManager {

	void start() throws ServiceStartException;
	
	boolean addAndStart(IService service) throws ServiceAlreadyExistsException, ServiceStartException;
	
	void stop() throws ServiceStopException;
	
	void removeAndStop(String id) throws ServiceStopException;
	
	<T extends IService> T getService(String id);
}
