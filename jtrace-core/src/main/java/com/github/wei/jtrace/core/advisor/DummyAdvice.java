package com.github.wei.jtrace.core.advisor;

import com.github.wei.jtrace.agent.IAdvice;

public class DummyAdvice implements IAdvice {

	@Override
	public void onBegin(Object[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onReturn(Object obj) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onThrow(Throwable thr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onInvoke(Integer lineNumber, String own, String name, String desc, boolean itf) {
		// TODO Auto-generated method stub
		
	}


}
