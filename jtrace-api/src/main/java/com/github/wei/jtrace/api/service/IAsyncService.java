package com.github.wei.jtrace.api.service;

public interface IAsyncService extends IService, Stoppable, Runnable{
	boolean isRunning();
}
