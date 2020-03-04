package com.github.wei.jtrace.api.service;

import com.github.wei.jtrace.api.exception.ServiceStopException;

public interface Stoppable {
	void stop() throws ServiceStopException;
}
