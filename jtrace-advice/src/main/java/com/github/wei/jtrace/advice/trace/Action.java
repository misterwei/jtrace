package com.github.wei.jtrace.advice.trace;

import java.util.Stack;

public class Action {
	private Stack<Trace> traceStack = new Stack<Trace>();
	private Trace firstTrace = null;
	private final long startTime;
	private ActionManager manager;
	private Thread thread;
	public Action(ActionManager manager) {
		this.manager = manager;
		this.startTime = System.currentTimeMillis();
		this.thread = Thread.currentThread();
	}
	
	public Trace createTrace(String name) {
		Trace parent = null;
		if(!traceStack.empty()) {
			parent = traceStack.peek();
		}
		
		Trace trace = new Trace(parent, name);
		trace.start();
		if(parent == null) {
			firstTrace = trace;
		}
		traceStack.push(trace);
		
		return trace;
	}
	
	public void finishTrace() {
		Trace trace = traceStack.pop();
		trace.end();
		if(traceStack.empty()) {
			finishAction();
		}
	}
	
	public Thread getThread() {
		return thread;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public Trace getFirstTrace() {
		return firstTrace;
	}
	
	private void finishAction() {
		manager.finishAction(this);
	}
}
