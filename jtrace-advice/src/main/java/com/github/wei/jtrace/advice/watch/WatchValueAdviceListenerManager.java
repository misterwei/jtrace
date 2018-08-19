package com.github.wei.jtrace.advice.watch;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.wei.jtrace.advice.ognl.OgnlMemberAccess;
import com.github.wei.jtrace.api.advice.AdviceMatcher;
import com.github.wei.jtrace.api.advice.IAdviceController;
import com.github.wei.jtrace.api.advice.IAdviceListener;
import com.github.wei.jtrace.api.advice.IAdviceListenerManager;

import ognl.Ognl;
import ognl.OgnlContext;

public class WatchValueAdviceListenerManager implements IAdviceListenerManager{
	private final String pos;
	private final String expr;
	private final int times;
	private final AdviceMatcher matcher;
	
	private List<Object> values = new CopyOnWriteArrayList<Object>();
	
	private ThreadLocal<OgnlContext> ognlContext = new ThreadLocal<OgnlContext>() {
		protected OgnlContext initialValue() {
			return new OgnlContext(null, null, new OgnlMemberAccess(true));
		};
	};
	
	public WatchValueAdviceListenerManager(AdviceMatcher matcher, String pos, String expr, int times) {
		this.pos = pos;
		this.expr = expr;
		this.times = times;
		this.matcher = matcher;
	}
	
	public List<Object> getResult(){
		return values;
	}
	
	private void addValue(Object obj){
		if(values.size() >= times) {
			return;
		}
		
		Object result = null;
		try {
			result = Ognl.getValue(expr, ognlContext.get(), obj);
		}catch(Exception e) {
			result = e;
		}
		
		values.add(result);
	}

	public IAdviceListener create(Class<?> ownClass, Object own, String methodName, String methodDescr, Object[] matcherMessage) {
		return new AdviceListener();
	}
	
	private class AdviceListener implements IAdviceListener{
		
		public Object[] onBegin(Object[] args) {
			if("begin".equals(pos)) {
				addValue(args);
			}
			return args;
		}

		public Object onReturn(Object obj) {
			if("return".equals(pos)) {
				addValue(obj);
			}
			return obj;
		}

		public void onThrow(Throwable thr) {
			if("throw".equals(pos)) {
				addValue(thr);
			}
		}

		public void onInvoke(Integer lineNumber, String own, String name, String desc, boolean itf) {
			
		}
	}

	@Override
	public void init(IAdviceController controller) {
		controller.addMatcher(matcher);
		controller.refresh();
	}

}
