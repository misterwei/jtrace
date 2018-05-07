package com.github.wei.jtrace.advice;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.api.advice.AdviceConfig;
import com.github.wei.jtrace.api.advice.IAdviceListenerManager;
import com.github.wei.jtrace.api.advice.IAdviceManager;
import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;

@Bean
public class AdviceService {
	static Logger log = LoggerFactory.getLogger("AdviceService");
	
	@AutoRef
	private IAdviceManager adviceManager;
	
	private AtomicInteger ids= new AtomicInteger(0);
	private ConcurrentHashMap<Integer, IAdviceListenerManager> adviceListeners = new ConcurrentHashMap<Integer, IAdviceListenerManager>();
	
	
	public int registAdviceListener(AdviceConfig config, IAdviceListenerManager listener, boolean relatParent) throws Exception {
		int id = ids.incrementAndGet();
		adviceListeners.put(id, listener);
		
		adviceManager.registAdviceListener(config, listener, relatParent);
		
		return id;
	}
	
	public void registAdviceListenerWithNoID(AdviceConfig config, IAdviceListenerManager listener, boolean relatParent) throws Exception {
		adviceManager.registAdviceListener(config, listener, relatParent);
	}
	
	public IAdviceListenerManager getAdviceListenerManager(int id) {
		return adviceListeners.get(id);
	}
	
	public IAdviceListenerManager removeAdviceListenerManager(int id) {
		IAdviceListenerManager manager = adviceListeners.remove(id);
		if(manager != null) {
			adviceManager.removeAdviceListener(manager);
		}
		return manager;
	}

	public Set<Integer> getRegistIds(){
		return adviceListeners.keySet();
	}
	
}
