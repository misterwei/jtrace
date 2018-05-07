package com.github.wei.jtrace.core.util;

import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {
	private final AtomicInteger counter;
	private final int START;
	private final int MAX_COUNT;
	
	private IdGenerator(int count) {
		this(1, count);
	}
	
	private IdGenerator(int start, int count) {
		this.counter = new AtomicInteger(start);
		this.START = start;
		this.MAX_COUNT = count;
	}
	
	public int next() {
		int id = counter.getAndIncrement();
		if(id + START > MAX_COUNT) {
			synchronized (counter) {
				if(counter.get() + START> MAX_COUNT) {
					counter.set(START);
				}
				return counter.getAndIncrement();
			}
		}
		return id;
	}
	
	public static IdGenerator generate(int max) {
		return new IdGenerator(max);
	}
	
}
