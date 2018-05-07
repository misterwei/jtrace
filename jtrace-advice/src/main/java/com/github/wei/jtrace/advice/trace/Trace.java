package com.github.wei.jtrace.advice.trace;

import java.util.ArrayList;
import java.util.List;

public class Trace {

	private List<Trace> childs = null;
	
	private String name;
	
	private long duration;
	private long startTime;
	
	public Trace(Trace parent, String name) {
		this.name = name;
		if(parent != null) {
			parent.addChild(this);
		}
	}

	private void addChild(Trace child) {
		if(childs == null) {
			childs = new ArrayList<Trace>();
		}
		childs.add(child);
	}
	
	public List<Trace> getChilds() {
		return childs;
	}

	public String getName() {
		return name;
	}
	
	public void start() {
		startTime = System.currentTimeMillis();
	}
	
	public void end() {
		duration = System.currentTimeMillis() - startTime;
	}

	public long getDuration() {
		return duration;
	}

	public long getStartTime() {
		return startTime;
	}
	
}
