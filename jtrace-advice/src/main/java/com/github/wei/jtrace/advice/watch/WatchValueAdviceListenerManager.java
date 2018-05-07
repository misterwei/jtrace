package com.github.wei.jtrace.advice.watch;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.wei.jtrace.advice.ognl.OgnlMemberAccess;
import com.github.wei.jtrace.api.advice.IAdviceListener;
import com.github.wei.jtrace.api.advice.IAdviceListenerManager;

import ognl.Ognl;
import ognl.OgnlContext;

public class WatchValueAdviceListenerManager implements IAdviceListenerManager{
	private final String pos;
	private final String expr;
	
	private List<Object> values = new CopyOnWriteArrayList<Object>();
	
	private ThreadLocal<OgnlContext> ognlContext = new ThreadLocal<OgnlContext>() {
		protected OgnlContext initialValue() {
			return new OgnlContext(null, null, new OgnlMemberAccess(true));
		};
	};
	
	public WatchValueAdviceListenerManager(String pos, String expr) {
		this.pos = pos;
		this.expr = expr;
	}
	
	public List<Object> getResult(){
		return values;
	}
	
	private void addValue(Object obj){
		Object result = null;
		try {
			result = Ognl.getValue(expr, ognlContext.get(), obj);
		}catch(Exception e) {
			result = e;
		}
		
		values.add(result);
	}

	public IAdviceListener create(Class<?> ownClass, Object own, String methodName, String methodDescr) {
		return new AdviceListener();
	}
	
	private class AdviceListener implements IAdviceListener{
		
		public void onBegin(Object[] args) {
			if("begin".equals(pos)) {
				addValue(args);
			}
		}

		public void onReturn(Object obj) {
			if("return".equals(pos)) {
				addValue(obj);
			}
		}

		public void onThrow(Throwable thr) {
			if("throw".equals(pos)) {
				addValue(thr);
			}
		}

		public void onInvoke(Integer lineNumber, String own, String name, String desc, boolean itf) {
			
		}
	}

}
