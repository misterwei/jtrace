package com.github.wei.jtrace.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.api.advice.IAdviceListenerManager;
import com.github.wei.jtrace.api.advice.IAdviceManager;
import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;

@Bean
public class AdviceService {
	static Logger log = LoggerFactory.getLogger("AdviceService");
	
	@AutoRef
	private IAdviceManager adviceManager;
	
	public int registAdviceListener(IAdviceListenerManager listener) throws Exception {
		return adviceManager.registAdviceListener(listener);
	}
	
	public IAdviceListenerManager removeAdviceListenerManager(int id) {
		return adviceManager.removeAdviceListener(id);
	}

	public IAdviceListenerManager getAdviceListenerManager(int id) {
		return adviceManager.getAdviceListener(id);
	}
}
