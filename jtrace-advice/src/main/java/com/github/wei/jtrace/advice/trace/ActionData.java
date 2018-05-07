package com.github.wei.jtrace.advice.trace;

public class ActionData {
	private Trace action = null;
	private long startTime;
	private String thread;
	
	public ActionData(Action act) {
		action = act.getFirstTrace();
		startTime = action.getStartTime();
		thread = act.getThread().getName();
	}
	
	public String getThread() {
		return thread;
	}
	
	public Trace getAction() {
		return action;
	}
	
	public long getStartTime() {
		return startTime;
	}
}
