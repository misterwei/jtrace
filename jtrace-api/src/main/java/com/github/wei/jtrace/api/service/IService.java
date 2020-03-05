package com.github.wei.jtrace.api.service;

import com.github.wei.jtrace.api.config.IConfig;
import com.github.wei.jtrace.api.exception.ServiceStartException;

public interface IService {
	
	String getId();
	
	boolean start(IConfig config) throws ServiceStartException;
	
}
