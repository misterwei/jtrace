package com.github.wei.jtrace.advice.trace;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ActionManager {
	private ThreadLocal<Action> currentAction = new ThreadLocal<Action>();
	
	private final List<Action> actions = new CopyOnWriteArrayList<Action>();
	
	public List<Action> getActions(){
		return actions;
	}
	
	public void clear(){
		actions.clear();
	}
	
	public Action getAction(boolean create) {
		Action action = currentAction.get();
		if(action == null) {
			if(create) {
				action = new Action(this);
				currentAction.set(action);
			}
		}
		return action;
	}
	
	public void finishAction(Action action) {
		if(currentAction.get() == action) {
			currentAction.remove();
		}
		actions.add(action);
	}
}
