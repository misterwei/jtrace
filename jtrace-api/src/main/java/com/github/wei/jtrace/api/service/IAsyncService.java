package com.github.wei.jtrace.api.service;

public interface IAsyncService extends IService, Stopable, Runnable{
	boolean isRunning();
}
