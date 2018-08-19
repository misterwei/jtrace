package com.github.wei.jtrace.advice.time;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.wei.jtrace.api.advice.AdviceMatcher;
import com.github.wei.jtrace.api.advice.IAdviceController;
import com.github.wei.jtrace.api.advice.IAdviceListener;
import com.github.wei.jtrace.api.advice.IAdviceListenerManager;

public class TimeCountAdviceListenerManager implements IAdviceListenerManager{
	private volatile long max = 0;
	private volatile long min = Integer.MAX_VALUE;
	private volatile long total = 0;
	
	private String className;
	private String method;
	private final int times;
	private List<Long> values = new CopyOnWriteArrayList<Long>();

	public TimeCountAdviceListenerManager(String className, String method, int times) {
		this.className = className;
		this.method = method;
		this.times = times;
	}
	
	public TimeCount computeTimeCount(){
		long count = values.size();
		long total = this.total;
		TimeCount timecount = new TimeCount();
		timecount.setAverage(total / count);
		timecount.setCount(count);
		timecount.setMax(max);
		timecount.setMin(min);
		timecount.setTotal(total);
		return timecount;
	}
	
	private synchronized void addValue(long time){
		if(values.size() >= times) {
			return;
		}
		
		if(time > max) {
			max = time;
		}
		if(time < min) {
			min = time;
		}
		total += time;
		values.add(time);
	}

	public IAdviceListener create(Class<?> ownClass, Object own, String methodName, String methodDescr, Object[] matcherMessage) {
		return new AdviceListener();
	}
	
	@Override
	public void init(IAdviceController controller) {
		controller.addMatcher(AdviceMatcher.newBuilder(className).addMethod(method).end().build());
		controller.refresh();
	}
	
	private class AdviceListener implements IAdviceListener{
		private long start = 0;
		
		public Object[] onBegin(Object[] args) {
			start = System.currentTimeMillis();
			return args;
		}

		public Object onReturn(Object obj) {
			addValue(System.currentTimeMillis() - start);
			return obj;
		}

		public void onThrow(Throwable thr) {
			addValue(System.currentTimeMillis() - start);
		}

		public void onInvoke(Integer lineNumber, String own, String name, String desc, boolean itfc) {
			
		}
	}

}
