package com.github.wei.jtrace.advice.trace;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.api.advice.AdviceMatcher;
import com.github.wei.jtrace.api.advice.AdviceMatcher.MatchType;
import com.github.wei.jtrace.api.advice.IAdviceController;
import com.github.wei.jtrace.api.advice.IAdviceListener;
import com.github.wei.jtrace.api.advice.IAdviceListenerManager;

public class TraceAdviceListenerManager implements IAdviceListenerManager{
	static Logger log = LoggerFactory.getLogger("TraceAdviceListenerManager");
	private List<String> tracedConfig = new CopyOnWriteArrayList<String>(); 
	
	private ActionManager actionManager;
	private AdviceMatcher matcher;
	private IAdviceController adviceController;
	
	public TraceAdviceListenerManager(AdviceMatcher matcher) {
		actionManager = new ActionManager();
		this.matcher = matcher;
	}
	
	public List<Action> getActions(){
		return actionManager.getActions();
	}
	
	public IAdviceListener create(Class<?> ownClass, Object own, String methodName, String methodDescr, Object[] matcherMessage) {
		boolean createAction = false;
		if(ownClass.getName().equals(matcher.getMatchClassName())) {
			createAction = true;
		}
		return new AdviceListener(ownClass, own, methodName, methodDescr, createAction);
	}
	
	private class AdviceListener implements IAdviceListener{
		private String own;
		private String method;
		private String desc;
		private boolean createAction;
		public AdviceListener(Class<?> ownClass, Object own, String methodName, String methodDescr, boolean createAction) {
			this.own = ownClass.getName();
			this.method = methodName;
			this.desc = methodDescr;
			this.createAction = createAction;
		}
		
		public Object[] onBegin(Object[] args) {
			Action action = actionManager.getAction(createAction);
			if(action != null) {
				action.createTrace(own+"."+method + desc);
			}
			return args;
		}

		public Object onReturn(Object obj) {
			Action action = actionManager.getAction(createAction);
			if(action != null) {
				action.finishTrace();
			}
			return obj;
		}

		public void onThrow(Throwable thr) {
			Action action = actionManager.getAction(createAction);
			if(action != null) {
				action.finishTrace();
			}
		}

		public void onInvoke(Integer lineNumber, String own, String name, String desc, boolean itf) {
			if(log.isDebugEnabled()) {
				log.debug("invoke ({}){}.{}{}", new Object[] {lineNumber, own, name, desc});
			}
			if(own.startsWith("java/") || own.startsWith("com/sun")  || own.startsWith("sun/")) {
				Action action = actionManager.getAction(createAction);
				if(action != null) {
					action.createTrace(own+"."+name + desc);
					action.finishTrace();
				}
				return;
			}
			
			String fullMethod = name+desc;
			String key = own + fullMethod;
			if(tracedConfig.contains(key)) {
				return;
			}
			tracedConfig.add(key);
			
			adviceController.addMatcher(AdviceMatcher.newBuilder(own).addMethod(fullMethod).withTrace().end().relateParent().matchType(MatchType.BASE).build());
			adviceController.refresh();
		}
	}

	@Override
	public void init(IAdviceController adviceController) {
		this.adviceController = adviceController;
		adviceController.addMatcher(matcher);
		adviceController.refresh();
	}
}
