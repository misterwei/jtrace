package com.github.wei.jtrace.advice.time;

import java.io.Serializable;

public class TimeCount implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long max;
	private long min;
	private long average;
	private long total;
	private long count;
	
	public long getMax() {
		return max;
	}
	public void setMax(long max) {
		this.max = max;
	}
	public long getMin() {
		return min;
	}
	public void setMin(long min) {
		this.min = min;
	}
	public long getAverage() {
		return average;
	}
	public void setAverage(long average) {
		this.average = average;
	}
	public long getTotal() {
		return total;
	}
	public void setTotal(long total) {
		this.total = total;
	}
	public long getCount() {
		return count;
	}
	public void setCount(long count) {
		this.count = count;
	}
	
	
}
