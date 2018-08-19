package com.github.wei.jtrace.api.transform.matcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 非线程安全
 * 
 * @author wei-8
 */
public class MatcherContext{
	private static final int DEFAULT_PRIORITY = 0;
	
	private HashMap<String, PriorityValue> context = new HashMap<String, PriorityValue>();
	private HashMap<String, List<Object>> context_list = new HashMap<String, List<Object>>();
	
	public MatcherContext() {}
	
	public MatcherContext(MatcherContext mc) {
		innerMerge(mc);
	}
	
	public boolean put(String key, Object value) {
		return compareAndSet(key, new PriorityValue(DEFAULT_PRIORITY, value));
	}
	
	public boolean put(String key, Object value, int priority) {
		return compareAndSet(key, new PriorityValue(priority, value));
	}
	
	public void addForList(String key, Object value) {
		List<Object> obj = getList(key);
		if(obj == null) {
			obj = new ArrayList<Object>();
			context_list.put(key, obj);
		}
		obj.add(value);
	}
	
	
	public List<Object> getList(String key){
		List<Object> v = context_list.get(key);
		if(v != null) {
			return v;
		}
		return null;
	}
	
	private void innerMerge(MatcherContext sec) {
		Set<Map.Entry<String, PriorityValue> > entrys = sec.context.entrySet();
		for(Map.Entry<String, PriorityValue> entry : entrys) {
			compareAndSet(entry.getKey(), entry.getValue());
		}
		
		Set<Map.Entry<String, List<Object>> > entrys_list = sec.context_list.entrySet();
		for(Map.Entry<String, List<Object>> entry : entrys_list) {
			String key = entry.getKey();
			List<Object> list = getList(key);
			if(list == null) {
				list = new ArrayList<Object>();
				this.context_list.put(key, list);
			}
			list.addAll(entry.getValue());
		}
	}
	
	public void merge(MatcherContext sec) {
		innerMerge(sec);
	}
	
	public void putAll(Map<String, Object> sec) {
		Set<Map.Entry<String, Object> > entrys = sec.entrySet();
		for(Map.Entry<String, Object> entry : entrys) {
			compareAndSet(entry.getKey(), new PriorityValue(DEFAULT_PRIORITY, entry.getValue()));
		}
	}
	
	public Object get(String key) {
		PriorityValue v = context.get(key);
		if(v != null) {
			return v.getValue();
		}
		return null;
	}
	
	public Object remove(String key) {
		PriorityValue v = context.remove(key);
		if(v != null) {
			return v.getValue();
		}
		return null;
	}

	public void clear() {
		this.context.clear();
		this.context_list.clear();
	}
	
	public MatcherContext clone() {
		return new MatcherContext(this);
	}
	
	public MatcherContext readonly() {
		return new MatcherContext(this) {
			
			@Override
			public void addForList(String key, Object value) {
				throw new UnsupportedOperationException("This operation (addForList) is not supported.");
			}
			
			@Override
			public boolean put(String key, Object value) {
				throw new UnsupportedOperationException("This operation (put) is not supported.");
			}
			
			@Override
			public boolean put(String key, Object value, int priority) {
				throw new UnsupportedOperationException("This operation (put) is not supported.");
			}
			
			@Override
			public void putAll(Map<String, Object> sec) {
				throw new UnsupportedOperationException("This operation (putAll) is not supported.");
			}
			
			@Override
			public Object remove(String key) {
				throw new UnsupportedOperationException("This operation (remove) is not supported.");

			}
			
			@Override
			public void merge(MatcherContext sec) {
				throw new UnsupportedOperationException("This operation (merge) is not supported.");
			}
			
			@Override
			public void clear() {
				throw new UnsupportedOperationException("This operation (clear) is not supported.");
			}
		};
	}
	
	private boolean compareAndSet(String key, PriorityValue value) {
		PriorityValue v = context.get(key);
		if(v == null) {
			context.put(key, value);
			return true;
		}else if(v.getPriority() <= value.getPriority()){
			context.put(key, value);
			return true;
		}
		return false;
	}
	
	private static class PriorityValue{
		private int priority;
		private Object value;
		public PriorityValue(int priority, Object value) {
			this.priority = priority;
			this.value = value;
		}
		
		public int getPriority() {
			return priority;
		}
		
		public Object getValue() {
			return value;
		}
	}
}
